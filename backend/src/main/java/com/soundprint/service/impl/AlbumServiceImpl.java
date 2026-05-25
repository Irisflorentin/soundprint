package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.album.AlbumCreateRequest;
import com.soundprint.dto.request.album.AlbumQueryRequest;
import com.soundprint.dto.request.album.AlbumUpdateRequest;
import com.soundprint.dto.response.AlbumDetailResponse;
import com.soundprint.dto.response.AlbumResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import com.soundprint.entity.Track;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.AlbumMapper;
import com.soundprint.mapper.ArtistMapper;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 专辑表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements AlbumService {

    private final ArtistMapper artistMapper;
    private final TrackMapper trackMapper;

    @Override
    public PageResult<AlbumResponse> pageQuery(AlbumQueryRequest query) {
        Page<Album> p = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Album> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.like(Album::getTitle, query.getKeyword());
        }
        if (query.getArtistId() != null) {
            w.eq(Album::getArtistId, query.getArtistId());
        }
        w.orderByDesc(Album::getCreatedAt);
        Page<Album> result = page(p, w);
        return PageResult.of(toResponses(result.getRecords()),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public AlbumDetailResponse getDetail(Long id) {
        Album album = getById(id);
        if (album == null) {
            throw new ResourceNotFoundException("专辑", id);
        }
        Artist artist = album.getArtistId() != null ? artistMapper.selectById(album.getArtistId()) : null;
        List<TrackResponse> tracks = trackMapper.listByAlbum(id);
        return AlbumDetailResponse.from(album, artist, tracks);
    }

    @Override
    public AlbumResponse create(AlbumCreateRequest request) {
        Album a = new Album();
        a.setTitle(request.getTitle());
        a.setArtistId(request.getArtistId());
        a.setCoverUrl(request.getCoverUrl());
        a.setReleaseYear(request.getReleaseYear());
        a.setGenre(request.getGenre());
        a.setDescription(request.getDescription());
        a.setIsDeleted((byte) 0);
        save(a);
        return AlbumResponse.from(a, loadArtist(a.getArtistId()));
    }

    @Override
    public AlbumResponse updateAlbum(Long id, AlbumUpdateRequest request) {
        Album a = getById(id);
        if (a == null) {
            throw new ResourceNotFoundException("专辑", id);
        }
        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getArtistId() != null) a.setArtistId(request.getArtistId());
        if (request.getCoverUrl() != null) a.setCoverUrl(request.getCoverUrl());
        if (request.getReleaseYear() != null) a.setReleaseYear(request.getReleaseYear());
        if (request.getGenre() != null) a.setGenre(request.getGenre());
        if (request.getDescription() != null) a.setDescription(request.getDescription());
        updateById(a);
        return AlbumResponse.from(a, loadArtist(a.getArtistId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlbum(Long id) {
        Album a = getById(id);
        if (a == null) {
            throw new ResourceNotFoundException("专辑", id);
        }
        // 软删除专辑前，把引用它的曲目 album_id 置空（软删不会触发 FK 的 ON DELETE SET NULL）
        trackMapper.update(null, new LambdaUpdateWrapper<Track>()
                .eq(Track::getAlbumId, id)
                .set(Track::getAlbumId, null));
        removeById(id);
    }

    // ====== 私有辅助 ======

    private List<AlbumResponse> toResponses(List<Album> albums) {
        if (albums == null || albums.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> artistIds = albums.stream()
                .map(Album::getArtistId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Artist> artistMap = artistIds.isEmpty() ? Collections.emptyMap()
                : artistMapper.selectBatchIds(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, Function.identity()));
        return albums.stream()
                .map(a -> AlbumResponse.from(a, artistMap.get(a.getArtistId())))
                .collect(Collectors.toList());
    }

    private Artist loadArtist(Long artistId) {
        return artistId != null ? artistMapper.selectById(artistId) : null;
    }
}
