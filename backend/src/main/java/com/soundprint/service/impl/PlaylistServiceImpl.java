package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.playlist.PlaylistCreateRequest;
import com.soundprint.dto.request.playlist.PlaylistReorderRequest;
import com.soundprint.dto.request.playlist.PlaylistUpdateRequest;
import com.soundprint.dto.response.PlaylistDetailResponse;
import com.soundprint.dto.response.PlaylistResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.Playlist;
import com.soundprint.entity.PlaylistTrack;
import com.soundprint.entity.Track;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.PlaylistMapper;
import com.soundprint.mapper.PlaylistTrackMapper;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.service.PlaylistService;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 歌单表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService {

    private final PlaylistTrackMapper playlistTrackMapper;
    private final TrackMapper trackMapper;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public PageResult<PlaylistResponse> pageQuery(Long page, Long size) {
        Long uid = currentUserUtil.getCurrentUserId();
        Page<Playlist> p = new Page<>(page, size);
        Page<Playlist> result = page(p, new LambdaQueryWrapper<Playlist>()
                .eq(Playlist::getUserId, uid)
                .orderByDesc(Playlist::getCreatedAt));

        // 一次查出这些歌单的所有曲目关系，在内存里按 playlist_id 统计数量（避免 N+1）
        List<Long> ids = result.getRecords().stream().map(Playlist::getId).toList();
        Map<Long, Long> countMap = ids.isEmpty() ? Collections.emptyMap()
                : playlistTrackMapper.selectList(new LambdaQueryWrapper<PlaylistTrack>()
                        .in(PlaylistTrack::getPlaylistId, ids)).stream()
                .collect(Collectors.groupingBy(PlaylistTrack::getPlaylistId, Collectors.counting()));

        List<PlaylistResponse> records = result.getRecords().stream()
                .map(pl -> PlaylistResponse.from(pl, countMap.getOrDefault(pl.getId(), 0L).intValue()))
                .collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PlaylistDetailResponse getDetail(Long id) {
        Playlist pl = getById(id);
        if (pl == null) {
            throw new ResourceNotFoundException("歌单", id);
        }
        List<TrackResponse> tracks = trackMapper.listByPlaylist(id);
        return PlaylistDetailResponse.from(pl, tracks);
    }

    @Override
    public PlaylistResponse create(PlaylistCreateRequest request) {
        Playlist pl = new Playlist();
        pl.setUserId(currentUserUtil.getCurrentUserId());
        pl.setName(request.getName());
        pl.setDescription(request.getDescription());
        pl.setCoverUrl(request.getCoverUrl());
        pl.setIsDeleted((byte) 0);
        save(pl);
        return PlaylistResponse.from(pl, 0);
    }

    @Override
    public PlaylistResponse updatePlaylist(Long id, PlaylistUpdateRequest request) {
        Playlist pl = getById(id);
        if (pl == null) {
            throw new ResourceNotFoundException("歌单", id);
        }
        if (request.getName() != null) pl.setName(request.getName());
        if (request.getDescription() != null) pl.setDescription(request.getDescription());
        if (request.getCoverUrl() != null) pl.setCoverUrl(request.getCoverUrl());
        updateById(pl);
        long count = playlistTrackMapper.selectCount(new LambdaQueryWrapper<PlaylistTrack>()
                .eq(PlaylistTrack::getPlaylistId, id));
        return PlaylistResponse.from(pl, (int) count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlaylist(Long id) {
        Playlist pl = getById(id);
        if (pl == null) {
            throw new ResourceNotFoundException("歌单", id);
        }
        // 关系表硬删除（中间记录），歌单本身软删除
        playlistTrackMapper.delete(new LambdaQueryWrapper<PlaylistTrack>().eq(PlaylistTrack::getPlaylistId, id));
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTrack(Long playlistId, Long trackId) {
        Playlist pl = getById(playlistId);
        if (pl == null) {
            throw new ResourceNotFoundException("歌单", playlistId);
        }
        if (trackMapper.selectById(trackId) == null) {
            throw new ResourceNotFoundException("曲目", trackId);
        }
        long exists = playlistTrackMapper.selectCount(new LambdaQueryWrapper<PlaylistTrack>()
                .eq(PlaylistTrack::getPlaylistId, playlistId)
                .eq(PlaylistTrack::getTrackId, trackId));
        if (exists > 0) {
            // 已存在不重复加，幂等返回
            return;
        }
        PlaylistTrack last = playlistTrackMapper.selectOne(new LambdaQueryWrapper<PlaylistTrack>()
                .eq(PlaylistTrack::getPlaylistId, playlistId)
                .orderByDesc(PlaylistTrack::getOrderIndex)
                .last("LIMIT 1"));
        int next = (last == null || last.getOrderIndex() == null) ? 0 : last.getOrderIndex() + 1;

        PlaylistTrack pt = new PlaylistTrack();
        pt.setPlaylistId(playlistId);
        pt.setTrackId(trackId);
        pt.setOrderIndex(next);
        pt.setAddedAt(LocalDateTime.now());
        playlistTrackMapper.insert(pt);
    }

    @Override
    public void removeTrack(Long playlistId, Long trackId) {
        playlistTrackMapper.delete(new LambdaQueryWrapper<PlaylistTrack>()
                .eq(PlaylistTrack::getPlaylistId, playlistId)
                .eq(PlaylistTrack::getTrackId, trackId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorder(Long playlistId, PlaylistReorderRequest request) {
        List<Long> trackIds = request.getTrackIds();
        // 单事务内按列表顺序重设 order_index，避免中间状态
        for (int i = 0; i < trackIds.size(); i++) {
            playlistTrackMapper.update(null, new LambdaUpdateWrapper<PlaylistTrack>()
                    .eq(PlaylistTrack::getPlaylistId, playlistId)
                    .eq(PlaylistTrack::getTrackId, trackIds.get(i))
                    .set(PlaylistTrack::getOrderIndex, i));
        }
    }
}
