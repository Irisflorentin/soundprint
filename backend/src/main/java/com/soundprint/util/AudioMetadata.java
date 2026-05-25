package com.soundprint.util;

import lombok.Data;

/**
 * 音频元数据 POJO
 * 对应从音频文件里能读出来的、可写入 track 表的字段。
 * 任何字段都可能为 null（文件没带该信息）。
 */
@Data
public class AudioMetadata {
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private String genre;
    private Integer durationSeconds;
    private Integer bitrateKbps;
    private Integer sampleRateHz;
    private Integer channels;
    private String lyrics;
    /** 内嵌封面的二进制数据，可能为 null */
    private byte[] coverImage;
}
