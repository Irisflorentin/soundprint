package com.soundprint.dto.request.track;

import lombok.Data;

/**
 * 曲目分页查询入参
 * GET 请求的查询参数会自动绑定到这个对象的字段上。
 */
@Data
public class TrackQueryRequest {
    /** 关键词（匹配标题/艺术家/专辑） */
    private String keyword;
    /** 按艺术家筛选 */
    private Long artistId;
    /** 按专辑筛选 */
    private Long albumId;
    /** 按格式筛选（FLAC/MP3...） */
    private String format;
    /** 页码，从 1 开始 */
    private Long page = 1L;
    /** 每页大小 */
    private Long size = 10L;
}
