package com.soundprint.controller;

import com.soundprint.common.Result;
import com.soundprint.dto.response.stats.GenreDistributionItem;
import com.soundprint.dto.response.stats.HeatmapItem;
import com.soundprint.dto.response.stats.MonthlyTrendItem;
import com.soundprint.dto.response.stats.StatsOverviewResponse;
import com.soundprint.dto.response.stats.TopArtistItem;
import com.soundprint.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计接口（ECharts 数据源）
 */
@Tag(name = "统计", description = "听歌统计聚合")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "统计总览")
    @GetMapping("/overview")
    public Result<StatsOverviewResponse> overview() {
        return Result.success(statsService.overview());
    }

    @Operation(summary = "流派分布")
    @GetMapping("/genres")
    public Result<List<GenreDistributionItem>> genres() {
        return Result.success(statsService.genres());
    }

    @Operation(summary = "Top 艺术家")
    @GetMapping("/top-artists")
    public Result<List<TopArtistItem>> topArtists(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statsService.topArtists(limit));
    }

    @Operation(summary = "月度播放趋势")
    @GetMapping("/monthly-trend")
    public Result<List<MonthlyTrendItem>> monthlyTrend(@RequestParam(defaultValue = "12") Integer months) {
        return Result.success(statsService.monthlyTrend(months));
    }

    @Operation(summary = "播放热力图")
    @GetMapping("/heatmap")
    public Result<List<HeatmapItem>> heatmap(@RequestParam(defaultValue = "365") Integer days) {
        return Result.success(statsService.heatmap(days));
    }
}
