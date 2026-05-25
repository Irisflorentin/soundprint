package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.artist.ArtistCreateRequest;
import com.soundprint.dto.request.artist.ArtistQueryRequest;
import com.soundprint.dto.request.artist.ArtistUpdateRequest;
import com.soundprint.dto.response.AlbumResponse;
import com.soundprint.dto.response.ArtistDetailResponse;
import com.soundprint.dto.response.ArtistResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.Album;
import com.soundprint.entity.Artist;
import com.soundprint.entity.Track;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.AlbumMapper;
import com.soundprint.mapper.ArtistMapper;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 艺术家表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements ArtistService {

    private final AlbumMapper albumMapper;
    private final TrackMapper trackMapper;

    @Override
    public PageResult<ArtistResponse> pageQuery(ArtistQueryRequest query) {
        Page<Artist> p = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Artist> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.like(Artist::getName, query.getKeyword());
        }
        w.orderByDesc(Artist::getCreatedAt);
        Page<Artist> result = page(p, w);
        List<ArtistResponse> records = result.getRecords().stream()
                .map(ArtistResponse::from).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ArtistDetailResponse getDetail(Long id) {
        Artist artist = getById(id);
        if (artist == null) {
            throw new ResourceNotFoundException("艺术家", id);
        }
        List<Album> albums = albumMapper.selectList(new LambdaQueryWrapper<Album>()
                .eq(Album::getArtistId, id)
                .orderByDesc(Album::getReleaseYear));
        List<AlbumResponse> albumResponses = albums.stream()
                .map(al -> AlbumResponse.from(al, artist)).collect(Collectors.toList());
        List<TrackResponse> tracks = trackMapper.listByArtist(id);
        return ArtistDetailResponse.from(artist, albumResponses, tracks);
    }

    @Override
    public ArtistResponse create(ArtistCreateRequest request) {
        Artist a = new Artist();
        a.setName(request.getName());
        a.setBio(request.getBio());
        a.setAvatarUrl(request.getAvatarUrl());
        a.setCountry(request.getCountry());
        a.setFormedYear(request.getFormedYear());
        a.setIsDeleted((byte) 0);
        save(a);
        return ArtistResponse.from(a);
    }

    @Override
    public ArtistResponse updateArtist(Long id, ArtistUpdateRequest request) {
        Artist a = getById(id);
        if (a == null) {
            throw new ResourceNotFoundException("艺术家", id);
        }
        if (request.getName() != null) a.setName(request.getName());
        if (request.getBio() != null) a.setBio(request.getBio());
        if (request.getAvatarUrl() != null) a.setAvatarUrl(request.getAvatarUrl());
        if (request.getCountry() != null) a.setCountry(request.getCountry());
        if (request.getFormedYear() != null) a.setFormedYear(request.getFormedYear());
        updateById(a);
        return ArtistResponse.from(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArtist(Long id) {
        Artist a = getById(id);
        if (a == null) {
            throw new ResourceNotFoundException("艺术家", id);
        }
        // 软删艺术家前，把引用它的专辑/曲目 artist_id 置空
        albumMapper.update(null, new LambdaUpdateWrapper<Album>()
                .eq(Album::getArtistId, id).set(Album::getArtistId, null));
        trackMapper.update(null, new LambdaUpdateWrapper<Track>()
                .eq(Track::getArtistId, id).set(Track::getArtistId, null));
        removeById(id);
    }
}
