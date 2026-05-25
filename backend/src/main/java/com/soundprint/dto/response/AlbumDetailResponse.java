package com.soundprint.dto.response;

import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 专辑详情响应 DTO（含曲目列表）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AlbumDetailResponse extends AlbumResponse {
    private Integer trackCount;
    private List<TrackResponse> tracks;

    public static AlbumDetailResponse from(Album a, Artist artist, List<TrackResponse> tracks) {
        AlbumDetailResponse r = new AlbumDetailResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setArtistId(a.getArtistId());
        r.setArtistName(artist != null ? artist.getName() : null);
        r.setCoverUrl(a.getCoverUrl());
        r.setReleaseYear(a.getReleaseYear());
        r.setGenre(a.getGenre());
        r.setDescription(a.getDescription());
        r.setCreatedAt(a.getCreatedAt());
        r.setTracks(tracks);
        r.setTrackCount(tracks != null ? tracks.size() : 0);
        return r;
    }
}
