package com.soundprint.dto.response;

import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import com.soundprint.entity.Tag;
import com.soundprint.entity.Track;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 曲目详情响应 DTO
 * 继承 TrackResponse，额外带歌词、专辑内编号、声道、标签列表、是否已收藏。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrackDetailResponse extends TrackResponse {
    private Integer trackNumber;
    private Integer channels;
    private String lyrics;
    private List<TagResponse> tags;
    private Boolean favorited;

    public static TrackDetailResponse from(Track t, Artist artist, Album album,
                                           List<Tag> tags, boolean favorited) {
        TrackDetailResponse r = new TrackDetailResponse();
        // —— 基类字段 ——
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
        // —— 详情扩展字段 ——
        r.setTrackNumber(t.getTrackNumber());
        r.setChannels(t.getChannels() == null ? null : t.getChannels().intValue());
        r.setLyrics(t.getLyrics());
        r.setTags(TagResponse.from(tags));
        r.setFavorited(favorited);
        return r;
    }
}
