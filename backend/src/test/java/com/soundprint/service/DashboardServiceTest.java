package com.soundprint.service;

import com.soundprint.common.PageResult;
import com.soundprint.dto.response.PlaylistResponse;
import com.soundprint.entity.User;
import com.soundprint.mapper.UserMapper;
import com.soundprint.util.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void getDashboardIncludesFeaturedPlaylists() {
        TrackService trackService = mock(TrackService.class);
        AlbumService albumService = mock(AlbumService.class);
        ArtistService artistService = mock(ArtistService.class);
        PlayHistoryService playHistoryService = mock(PlayHistoryService.class);
        UserFavoriteService userFavoriteService = mock(UserFavoriteService.class);
        PlaylistService playlistService = mock(PlaylistService.class);
        UserMapper userMapper = mock(UserMapper.class);
        CurrentUserUtil currentUserUtil = mock(CurrentUserUtil.class);

        PlaylistResponse playlist = new PlaylistResponse();
        playlist.setId(7L);
        playlist.setName("夜间驾驶");

        when(currentUserUtil.getCurrentUserId()).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(new User());
        when(trackService.pageQuery(org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResult.of(List.of(), 0L, 1L, 6L));
        when(userFavoriteService.listFavorites(1L, 6L))
                .thenReturn(PageResult.of(List.of(), 0L, 1L, 6L));
        when(albumService.pageQuery(org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResult.of(List.of(), 0L, 1L, 12L));
        when(artistService.pageQuery(org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResult.of(List.of(), 0L, 1L, 12L));
        when(playHistoryService.recent(8)).thenReturn(List.of());
        when(playlistService.pageQuery(1L, 12L))
                .thenReturn(PageResult.of(List.of(playlist), 1L, 1L, 12L));

        DashboardService service = new DashboardService(
                trackService,
                albumService,
                artistService,
                playHistoryService,
                userFavoriteService,
                playlistService,
                userMapper,
                currentUserUtil
        );

        assertThat(service.getDashboard().getFeaturedPlaylists())
                .extracting(PlaylistResponse::getName)
                .containsExactly("夜间驾驶");
    }
}
