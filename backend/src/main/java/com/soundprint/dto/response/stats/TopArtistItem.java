package com.soundprint.dto.response.stats;

import lombok.Data;

/**
 * Top 艺术家项（按播放次数）
 */
@Data
public class TopArtistItem {
    private Long artistId;
    private String artistName;
    private Long playCount;
}
