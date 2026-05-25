package com.soundprint.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 播放历史响应 DTO（最近播放用，由 JOIN 查询直接映射）
 */
@Data
public class PlayHistoryResponse {
    private Long trackId;
    private String title;
    private String artistName;
    private String albumTitle;
    private String coverUrl;
    private Integer playedSeconds;
    private LocalDateTime playedAt;
}
