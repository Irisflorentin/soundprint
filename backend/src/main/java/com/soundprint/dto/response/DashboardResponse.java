package com.soundprint.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 首页聚合响应 DTO
 */
@Data
public class DashboardResponse {
    private String greeting;
    private List<TrackResponse> recentTracks;
    private List<PlayHistoryResponse> recentlyPlayed;
    private List<TrackResponse> favorites;
    private List<AlbumResponse> featuredAlbums;
    private List<ArtistResponse> featuredArtists;
    private List<PlaylistResponse> featuredPlaylists;
}
