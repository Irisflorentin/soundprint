package com.soundprint.dto.response.stats;

import lombok.Data;

/**
 * 统计总览 DTO
 */
@Data
public class StatsOverviewResponse {
    private Long totalTracks;          // 曲目总数
    private Long totalDurationSeconds; // 曲库总时长（秒）
    private Long totalPlays;           // 累计播放次数
    private Long totalPlayedSeconds;   // 累计播放时长（秒）
    private Long favoriteCount;        // 收藏数
    private Long playlistCount;        // 歌单数
}
