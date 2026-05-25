package com.soundprint.dto.request.artist;

import lombok.Data;

/**
 * 艺术家分页查询入参
 */
@Data
public class ArtistQueryRequest {
    /** 关键词（匹配艺术家名） */
    private String keyword;
    private Long page = 1L;
    private Long size = 10L;
}
