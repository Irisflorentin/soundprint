package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.playlist.PlaylistCreateRequest;
import com.soundprint.dto.request.playlist.PlaylistReorderRequest;
import com.soundprint.dto.request.playlist.PlaylistUpdateRequest;
import com.soundprint.dto.response.PlaylistDetailResponse;
import com.soundprint.dto.response.PlaylistResponse;
import com.soundprint.entity.Playlist;

/**
 * <p>
 * 歌单表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface PlaylistService extends IService<Playlist> {

    PageResult<PlaylistResponse> pageQuery(Long page, Long size);

    PlaylistDetailResponse getDetail(Long id);

    PlaylistResponse create(PlaylistCreateRequest request);

    PlaylistResponse updatePlaylist(Long id, PlaylistUpdateRequest request);

    void deletePlaylist(Long id);

    /** 加歌（order_index 自动取当前最大值 + 1） */
    void addTrack(Long playlistId, Long trackId);

    /** 移除歌 */
    void removeTrack(Long playlistId, Long trackId);

    /** 按给定顺序重排 */
    void reorder(Long playlistId, PlaylistReorderRequest request);
}
