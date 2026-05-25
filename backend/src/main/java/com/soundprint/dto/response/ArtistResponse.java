package com.soundprint.dto.response;

import com.soundprint.entity.Artist;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 艺术家响应 DTO（列表用）
 */
@Data
public class ArtistResponse {
    private Long id;
    private String name;
    private String bio;
    private String avatarUrl;
    private String country;
    private Integer formedYear;
    private LocalDateTime createdAt;

    public static ArtistResponse from(Artist a) {
        ArtistResponse r = new ArtistResponse();
        r.setId(a.getId());
        r.setName(a.getName());
        r.setBio(a.getBio());
        r.setAvatarUrl(a.getAvatarUrl());
        r.setCountry(a.getCountry());
        r.setFormedYear(a.getFormedYear());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
