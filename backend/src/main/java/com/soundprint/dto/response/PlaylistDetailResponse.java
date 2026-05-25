package com.soundprint.dto.response;

import com.soundprint.entity.Playlist;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 歌单详情响应 DTO（含曲目列表，按歌单内排序）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlaylistDetailResponse extends PlaylistResponse {
    private List<TrackResponse> tracks;

    public static PlaylistDetailResponse from(Playlist p, List<TrackResponse> tracks) {
        PlaylistDetailResponse r = new PlaylistDetailResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setCoverUrl(p.getCoverUrl());
        r.setCreatedAt(p.getCreatedAt());
        r.setTracks(tracks);
        r.setTrackCount(tracks != null ? tracks.size() : 0);
        return r;
    }
}
