package com.soundprint.dto.response;

import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专辑响应 DTO（列表用）
 */
@Data
public class AlbumResponse {
    private Long id;
    private String title;
    private Long artistId;
    private String artistName;
    private String coverUrl;
    private Integer releaseYear;
    private String genre;
    private String description;
    private LocalDateTime createdAt;

    public static AlbumResponse from(Album a, Artist artist) {
        AlbumResponse r = new AlbumResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setArtistId(a.getArtistId());
        r.setArtistName(artist != null ? artist.getName() : null);
        r.setCoverUrl(a.getCoverUrl());
        r.setReleaseYear(a.getReleaseYear());
        r.setGenre(a.getGenre());
        r.setDescription(a.getDescription());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
