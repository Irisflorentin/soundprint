package com.soundprint.mapper;

import com.soundprint.dto.response.stats.GenreDistributionItem;
import com.soundprint.dto.response.stats.HeatmapItem;
import com.soundprint.dto.response.stats.MonthlyTrendItem;
import com.soundprint.dto.response.stats.TopArtistItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计聚合 Mapper（非实体 Mapper，专放聚合查询）
 *
 * 设计要点：所有聚合（COUNT/SUM/GROUP BY）都在数据库里完成，应用只拿结果。
 * 如果把全部播放记录查出来在 Java 里 groupBy，数据量大时会爆内存、网络也慢。
 */
@Mapper
public interface StatsMapper {

    /** 曲库总时长（秒） */
    Long sumTrackDuration();

    /** 累计播放次数 */
    Long countPlays(@Param("userId") Long userId);

    /** 累计播放时长（秒） */
    Long sumPlayedSeconds(@Param("userId") Long userId);

    /** 流派分布 */
    List<GenreDistributionItem> genreDistribution(@Param("userId") Long userId);

    /** Top 艺术家（按播放次数） */
    List<TopArtistItem> topArtists(@Param("userId") Long userId, @Param("limit") Integer limit);

    /** 最近 N 个月每月播放趋势 */
    List<MonthlyTrendItem> monthlyTrend(@Param("userId") Long userId, @Param("months") Integer months);

    /** 最近 N 天每天播放热力图 */
    List<HeatmapItem> heatmap(@Param("userId") Long userId, @Param("days") Integer days);
}
