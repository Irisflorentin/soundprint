package com.soundprint.dto.request.album;

import lombok.Data;

/**
 * 专辑分页查询入参
 */
@Data
public class AlbumQueryRequest {
    /** 关键词（匹配专辑名） */
    private String keyword;
    /** 按艺术家筛选 */
    private Long artistId;
    private Long page = 1L;
    private Long size = 10L;
}
