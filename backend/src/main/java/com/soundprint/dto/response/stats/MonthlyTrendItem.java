package com.soundprint.dto.response.stats;

import lombok.Data;

/**
 * 月度播放趋势项
 */
@Data
public class MonthlyTrendItem {
    private String month;        // 形如 2026-05
    private Long playCount;       // 当月播放次数
    private Long totalSeconds;    // 当月播放总秒数
}
