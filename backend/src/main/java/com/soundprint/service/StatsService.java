package com.soundprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soundprint.dto.response.stats.GenreDistributionItem;
import com.soundprint.dto.response.stats.HeatmapItem;
import com.soundprint.dto.response.stats.MonthlyTrendItem;
import com.soundprint.dto.response.stats.StatsOverviewResponse;
import com.soundprint.dto.response.stats.TopArtistItem;
import com.soundprint.entity.Playlist;
import com.soundprint.entity.UserFavorite;
import com.soundprint.mapper.StatsMapper;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 统计聚合服务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final UserFavoriteService userFavoriteService;
    private final CurrentUserUtil currentUserUtil;

    public StatsOverviewResponse overview() {
        Long uid = currentUserUtil.getCurrentUserId();
        StatsOverviewResponse r = new StatsOverviewResponse();
        r.setTotalTracks(trackService.count());
        r.setTotalDurationSeconds(statsMapper.sumTrackDuration());
        r.setTotalPlays(statsMapper.countPlays(uid));
        r.setTotalPlayedSeconds(statsMapper.sumPlayedSeconds(uid));
        r.setFavoriteCount(userFavoriteService.count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, uid)));
        r.setPlaylistCount(playlistService.count(new LambdaQueryWrapper<Playlist>()
                .eq(Playlist::getUserId, uid)));
        return r;
    }

    public List<GenreDistributionItem> genres() {
        List<GenreDistributionItem> list = statsMapper.genreDistribution(currentUserUtil.getCurrentUserId());
        long total = list.stream().mapToLong(GenreDistributionItem::getCount).sum();
        list.forEach(i -> i.setPercentage(total == 0 ? 0.0
                : Math.round(i.getCount() * 1000.0 / total) / 10.0));
        return list;
    }

    public List<TopArtistItem> topArtists(Integer limit) {
        return statsMapper.topArtists(currentUserUtil.getCurrentUserId(), limit);
    }

    public List<MonthlyTrendItem> monthlyTrend(Integer months) {
        return statsMapper.monthlyTrend(currentUserUtil.getCurrentUserId(), months);
    }

    public List<HeatmapItem> heatmap(Integer days) {
        return statsMapper.heatmap(currentUserUtil.getCurrentUserId(), days);
    }
}
