package com.soundprint.dto.response;

import com.soundprint.entity.Playlist;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 歌单响应 DTO（列表用）
 */
@Data
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Integer trackCount;
    private LocalDateTime createdAt;

    public static PlaylistResponse from(Playlist p, int trackCount) {
        PlaylistResponse r = new PlaylistResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setCoverUrl(p.getCoverUrl());
        r.setTrackCount(trackCount);
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
