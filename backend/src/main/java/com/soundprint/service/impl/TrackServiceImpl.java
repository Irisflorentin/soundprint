package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.track.TrackQueryRequest;
import com.soundprint.dto.request.track.TrackUpdateRequest;
import com.soundprint.dto.response.TrackDetailResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.*;
import com.soundprint.exception.BusinessException;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.*;
import com.soundprint.service.TrackService;
import com.soundprint.util.AudioMetadata;
import com.soundprint.util.AudioMetadataReader;
import com.soundprint.util.CurrentUserUtil;
import com.soundprint.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 曲目表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackServiceImpl extends ServiceImpl<TrackMapper, Track> implements TrackService {

    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final TagMapper tagMapper;
    private final TrackTagMapper trackTagMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final FileStorageUtil fileStorageUtil;
    private final AudioMetadataReader audioMetadataReader;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public PageResult<TrackResponse> pageQuery(TrackQueryRequest query) {
        Page<TrackResponse> page = new Page<>(query.getPage(), query.getSize());
        // baseMapper 即 TrackMapper，pageWithRelations 走 LEFT JOIN，一次查询避免 N+1
        IPage<TrackResponse> result = baseMapper.pageWithRelations(
                page, currentUserUtil.getCurrentUserId(),
                query.getKeyword(), query.getArtistId(), query.getAlbumId(), query.getFormat());
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public TrackDetailResponse getDetail(Long id) {
        Track t = getById(id);   // MP 自动过滤 is_deleted=0
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        Artist artist = t.getArtistId() != null ? artistMapper.selectById(t.getArtistId()) : null;
        Album album = t.getAlbumId() != null ? albumMapper.selectById(t.getAlbumId()) : null;
        List<Tag> tags = getTagsOfTrack(id);
        boolean favorited = isFavorited(currentUserUtil.getCurrentUserId(), id);
        return TrackDetailResponse.from(t, artist, album, tags, favorited);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrackResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (!fileStorageUtil.isAllowedAudioFormat(file.getOriginalFilename())) {
            throw new BusinessException("不支持的音频格式，仅支持 flac/mp3/wav/aac/ogg/m4a");
        }

        // 1) 先落盘
        String audioPath = fileStorageUtil.store(file, "audio");
        String coverPath = null;
        try {
            // 2) 读元数据
            File saved = fileStorageUtil.getFile(audioPath);
            AudioMetadata meta = audioMetadataReader.read(saved);

            // 3~5) 处理艺术家、专辑（找不到就新建）
            Long artistId = resolveArtist(meta.getArtist());
            Long albumId = resolveAlbum(meta.getAlbum(), artistId, meta);

            // 6) 处理内嵌封面
            if (meta.getCoverImage() != null && meta.getCoverImage().length > 0) {
                coverPath = fileStorageUtil.storeBytes(meta.getCoverImage(), "cover", "jpg");
            }

            // 7) 组装曲目并入库
            Track t = new Track();
            t.setTitle(meta.getTitle() != null ? meta.getTitle() : stripExtension(file.getOriginalFilename()));
            t.setArtistId(artistId);
            t.setAlbumId(albumId);
            t.setFilePath(audioPath);
            t.setCoverUrl(coverPath);
            t.setFormat(fileStorageUtil.getExtension(file.getOriginalFilename()).toUpperCase());
            t.setDurationSeconds(meta.getDurationSeconds());
            t.setBitrateKbps(meta.getBitrateKbps());
            t.setSampleRateHz(meta.getSampleRateHz());
            t.setChannels(meta.getChannels() != null ? meta.getChannels().byteValue() : null);
            t.setFileSizeBytes(file.getSize());
            t.setLyrics(meta.getLyrics());
            t.setIsDeleted((byte) 0);
            save(t);

            Artist artist = artistId != null ? artistMapper.selectById(artistId) : null;
            Album album = albumId != null ? albumMapper.selectById(albumId) : null;
            return TrackResponse.from(t, artist, album);
        } catch (Exception e) {
            // 任意步骤失败：删除已落盘的文件（DB 由 @Transactional 回滚）
            fileStorageUtil.delete(audioPath);
            fileStorageUtil.delete(coverPath);
            if (e instanceof BusinessException) {
                throw e;
            }
            log.error("上传处理失败", e);
            throw new BusinessException("上传处理失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrackResponse updateMetadata(Long id, TrackUpdateRequest request) {
        Track t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        if (request.getTitle() != null) t.setTitle(request.getTitle());
        if (request.getArtistId() != null) t.setArtistId(request.getArtistId());
        if (request.getAlbumId() != null) t.setAlbumId(request.getAlbumId());
        if (request.getTrackNumber() != null) t.setTrackNumber(request.getTrackNumber());
        if (request.getLyrics() != null) t.setLyrics(request.getLyrics());
        updateById(t);

        Artist artist = t.getArtistId() != null ? artistMapper.selectById(t.getArtistId()) : null;
        Album album = t.getAlbumId() != null ? albumMapper.selectById(t.getAlbumId()) : null;
        return TrackResponse.from(t, artist, album);
    }

    @Override
    public void deleteTrack(Long id) {
        Track t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        // 逻辑删除（is_deleted=1）。物理文件留待后台任务清理，避免误删后无法恢复
        removeById(id);
    }

    @Override
    public PageResult<TrackResponse> search(String keyword, Long page, Long size) {
        TrackQueryRequest q = new TrackQueryRequest();
        q.setKeyword(keyword);
        q.setPage(page);
        q.setSize(size);
        return pageQuery(q);
    }

    @Override
    public File getAudioFile(Long id) {
        Track t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        if (t.getFilePath() == null) {
            throw new BusinessException("该曲目没有关联音频文件");
        }
        File f = fileStorageUtil.getFile(t.getFilePath());
        if (!f.exists()) {
            // 种子数据 file_path 是占位路径，文件不存在 → 404，属预期
            throw new BusinessException(404, "音频文件不存在: " + t.getFilePath());
        }
        return f;
    }

    @Override
    public void updateLyrics(Long id, String lyrics) {
        Track t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        t.setLyrics(lyrics);
        updateById(t);
    }

    @Override
    public String getLyrics(Long id) {
        Track t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("曲目", id);
        }
        return t.getLyrics();
    }

    // ============ 私有辅助 ============

    /** 查曲目的标签列表 */
    private List<Tag> getTagsOfTrack(Long trackId) {
        List<TrackTag> rels = trackTagMapper.selectList(
                new LambdaQueryWrapper<TrackTag>().eq(TrackTag::getTrackId, trackId));
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = rels.stream().map(TrackTag::getTagId).toList();
        return tagMapper.selectBatchIds(tagIds);
    }

    /** 是否已被该用户收藏（复合主键，用 Wrapper 不能用 getById） */
    private boolean isFavorited(Long userId, Long trackId) {
        Long count = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTrackId, trackId));
        return count != null && count > 0;
    }

    /** 找艺术家，没有则新建，返回 id；名字为空返回 null */
    private Long resolveArtist(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Artist existing = artistMapper.selectOne(new LambdaQueryWrapper<Artist>()
                .eq(Artist::getName, name).last("LIMIT 1"));
        if (existing != null) {
            return existing.getId();
        }
        Artist a = new Artist();
        a.setName(name);
        a.setIsDeleted((byte) 0);
        artistMapper.insert(a);
        return a.getId();
    }

    /** 找专辑（同艺术家下 title 唯一），没有则新建，返回 id；名字为空返回 null */
    private Long resolveAlbum(String albumName, Long artistId, AudioMetadata meta) {
        if (albumName == null || albumName.isBlank()) {
            return null;
        }
        LambdaQueryWrapper<Album> w = new LambdaQueryWrapper<Album>().eq(Album::getTitle, albumName);
        if (artistId != null) {
            w.eq(Album::getArtistId, artistId);
        }
        w.last("LIMIT 1");
        Album existing = albumMapper.selectOne(w);
        if (existing != null) {
            return existing.getId();
        }
        Album al = new Album();
        al.setTitle(albumName);
        al.setArtistId(artistId);
        al.setReleaseYear(meta.getYear());
        al.setGenre(meta.getGenre());
        al.setIsDeleted((byte) 0);
        albumMapper.insert(al);
        return al.getId();
    }

    private String stripExtension(String filename) {
        if (filename == null) return "未命名";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
