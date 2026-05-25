package com.soundprint.dto.response.stats;

import lombok.Data;

/**
 * 热力图项（每天一个格子，GitHub 贡献图样式）
 */
@Data
public class HeatmapItem {
    private String date;          // 形如 2026-05-25
    private Long count;           // 当天播放次数
    private Long totalSeconds;    // 当天播放总秒数
}
