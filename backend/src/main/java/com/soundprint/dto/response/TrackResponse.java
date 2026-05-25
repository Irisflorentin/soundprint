package com.soundprint.dto.response;

import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import com.soundprint.entity.Track;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 曲目响应 DTO（列表用）
 *
 * 注意字段命名与 TrackMapper.xml 的 pageWithRelations 查询别名一致
 * （duration / bitrate / sampleRate），这样 JOIN 查询能直接映射。
 */
@Data
public class TrackResponse {
    private Long id;
    private String title;
    private Long artistId;
    private String artistName;
    private Long albumId;
    private String albumTitle;
    private String albumCoverUrl;
    private String coverUrl;
    private String format;
    private Integer duration;      // 时长（秒）
    private Integer bitrate;       // 比特率（kbps）
    private Integer sampleRate;    // 采样率（Hz）
    private Long fileSizeBytes;    // 文件大小（字节）
    private Boolean favorited;     // 当前用户是否已收藏（仅 JOIN 查询路径填充）
    private LocalDateTime createdAt;

    /** 由 Entity 手工组装（关联对象可为 null） */
    public static TrackResponse from(Track t, Artist artist, Album album) {
        TrackResponse r = new TrackResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setArtistId(t.getArtistId());
        r.setArtistName(artist != null ? artist.getName() : null);
        r.setAlbumId(t.getAlbumId());
        r.setAlbumTitle(album != null ? album.getTitle() : null);
        r.setAlbumCoverUrl(album != null ? album.getCoverUrl() : null);
        r.setCoverUrl(t.getCoverUrl());
        r.setFormat(t.getFormat());
        r.setDuration(t.getDurationSeconds());
        r.setBitrate(t.getBitrateKbps());
        r.setSampleRate(t.getSampleRateHz());
        r.setFileSizeBytes(t.getFileSizeBytes());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}
