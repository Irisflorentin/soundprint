package com.soundprint.dto.response;

import com.soundprint.entity.Artist;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 艺术家详情响应 DTO（含专辑列表 + 曲目列表）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArtistDetailResponse extends ArtistResponse {
    private Integer albumCount;
    private Integer trackCount;
    private List<AlbumResponse> albums;
    private List<TrackResponse> tracks;

    public static ArtistDetailResponse from(Artist a, List<AlbumResponse> albums, List<TrackResponse> tracks) {
        ArtistDetailResponse r = new ArtistDetailResponse();
        r.setId(a.getId());
        r.setName(a.getName());
        r.setBio(a.getBio());
        r.setAvatarUrl(a.getAvatarUrl());
        r.setCountry(a.getCountry());
        r.setFormedYear(a.getFormedYear());
        r.setCreatedAt(a.getCreatedAt());
        r.setAlbums(albums);
        r.setTracks(tracks);
        r.setAlbumCount(albums != null ? albums.size() : 0);
        r.setTrackCount(tracks != null ? tracks.size() : 0);
        return r;
    }
}
