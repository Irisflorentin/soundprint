package com.soundprint.util;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 音频元数据读取工具（封装 jaudiotagger）
 *
 * jaudiotagger 统一了不同格式的标签读取：
 *  - MP3 用 ID3v1/ID3v2
 *  - FLAC/OGG 用 Vorbis Comment
 *  - MP4/AAC 用 iTunes 风格的 atom
 * 它把这些差异屏蔽成统一的 FieldKey 枚举（TITLE/ARTIST/ALBUM...）。
 */
@Slf4j
@Component
public class AudioMetadataReader {

    private static final Pattern DIGITS = Pattern.compile("(\\d+)");

    /**
     * 读取音频文件元数据。
     * 损坏文件 / 不支持格式不会抛异常，而是返回已读到的部分（容错）。
     */
    public AudioMetadata read(File file) {
        AudioMetadata m = new AudioMetadata();
        try {
            AudioFile audioFile = AudioFileIO.read(file);

            // 1) 音频头：时长、比特率、采样率、声道（这些不在 tag 里，在编码头里）
            AudioHeader header = audioFile.getAudioHeader();
            if (header != null) {
                m.setDurationSeconds(header.getTrackLength());
                m.setBitrateKbps(parseLeadingInt(header.getBitRate()));   // 可能是 "~320"（VBR）
                m.setSampleRateHz(parseLeadingInt(header.getSampleRate()));
                m.setChannels(parseChannels(header.getChannels()));
            }

            // 2) 标签：标题、艺术家、专辑、流派、年份、歌词、封面
            Tag tag = audioFile.getTag();
            if (tag != null) {
                m.setTitle(blankToNull(safeGet(tag, FieldKey.TITLE)));
                m.setArtist(blankToNull(safeGet(tag, FieldKey.ARTIST)));
                m.setAlbum(blankToNull(safeGet(tag, FieldKey.ALBUM)));
                m.setGenre(blankToNull(safeGet(tag, FieldKey.GENRE)));
                m.setYear(parseLeadingIntObj(safeGet(tag, FieldKey.YEAR)));
                m.setLyrics(blankToNull(safeGet(tag, FieldKey.LYRICS)));
                try {
                    Artwork art = tag.getFirstArtwork();
                    if (art != null) {
                        m.setCoverImage(art.getBinaryData());
                    }
                } catch (Exception ignore) {
                    // 某些文件封面读取会抛，忽略，封面非必需
                }
            }
        } catch (Exception e) {
            log.warn("读取音频元数据失败（返回部分结果）: file={}, err={}", file.getName(), e.getMessage());
        }
        return m;
    }

    private String safeGet(Tag tag, FieldKey key) {
        try {
            return tag.getFirst(key);
        } catch (Exception e) {
            return null;
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /** 取字符串里第一段数字（如 "~320" → 320，"44100 Hz" → 44100），失败返回 0 */
    private int parseLeadingInt(String s) {
        Integer v = parseLeadingIntObj(s);
        return v == null ? 0 : v;
    }

    private Integer parseLeadingIntObj(String s) {
        if (s == null) return null;
        Matcher matcher = DIGITS.matcher(s);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    /** 声道字符串各格式不一："Stereo"/"Mono"/"2"/"1"，统一成数字 */
    private Integer parseChannels(String s) {
        if (s == null) return null;
        String lower = s.toLowerCase();
        if (lower.contains("stereo") || lower.contains("2")) return 2;
        if (lower.contains("mono") || lower.contains("1")) return 1;
        return parseLeadingIntObj(s);
    }
}
