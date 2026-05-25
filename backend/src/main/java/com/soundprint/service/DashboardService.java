package com.soundprint.service;

import com.soundprint.dto.request.album.AlbumQueryRequest;
import com.soundprint.dto.request.artist.ArtistQueryRequest;
import com.soundprint.dto.request.track.TrackQueryRequest;
import com.soundprint.dto.response.DashboardResponse;
import com.soundprint.entity.User;
import com.soundprint.mapper.UserMapper;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * 首页聚合服务（非实体服务，组合多个业务服务的数据）
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TrackService trackService;
    private final AlbumService albumService;
    private final ArtistService artistService;
    private final PlayHistoryService playHistoryService;
    private final UserFavoriteService userFavoriteService;
    private final UserMapper userMapper;
    private final CurrentUserUtil currentUserUtil;

    public DashboardResponse getDashboard() {
        DashboardResponse r = new DashboardResponse();
        r.setGreeting(buildGreeting());

        TrackQueryRequest tq = new TrackQueryRequest();
        tq.setPage(1L);
        tq.setSize(6L);
        r.setRecentTracks(trackService.pageQuery(tq).getRecords());

        r.setRecentlyPlayed(playHistoryService.recent(8));
        r.setFavorites(userFavoriteService.listFavorites(1L, 6L).getRecords());

        AlbumQueryRequest aq = new AlbumQueryRequest();
        aq.setPage(1L);
        aq.setSize(12L);
        r.setFeaturedAlbums(albumService.pageQuery(aq).getRecords());

        ArtistQueryRequest arq = new ArtistQueryRequest();
        arq.setPage(1L);
        arq.setSize(12L);
        r.setFeaturedArtists(artistService.pageQuery(arq).getRecords());

        return r;
    }

    private String buildGreeting() {
        int hour = LocalTime.now().getHour();
        String part = hour < 12 ? "上午好" : (hour < 18 ? "下午好" : "晚上好");
        User user = userMapper.selectById(currentUserUtil.getCurrentUserId());
        String nick = (user != null && user.getNickname() != null) ? user.getNickname() : "";
        return nick.isEmpty() ? part : part + "，" + nick;
    }
}
