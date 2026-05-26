package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.track.TrackQueryRequest;
import com.soundprint.dto.request.track.TrackUpdateRequest;
import com.soundprint.dto.response.TrackDetailResponse;
import com.soundprint.dto.response.TrackPeaksResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.Track;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * <p>
 * 曲目表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface TrackService extends IService<Track> {

    /** 分页查询曲目（含艺术家、专辑信息） */
    PageResult<TrackResponse> pageQuery(TrackQueryRequest query);

    /** 查询曲目详情（含歌词、标签、收藏状态） */
    TrackDetailResponse getDetail(Long id);

    /** 上传音频文件 → 自动读取元数据 → 入库 */
    TrackResponse upload(MultipartFile file);

    /** 更新曲目元数据（标题/艺术家/专辑等） */
    TrackResponse updateMetadata(Long id, TrackUpdateRequest request);

    /** 软删除曲目（数据库 is_deleted = 1，物理文件不立即删） */
    void deleteTrack(Long id);

    /** 模糊搜索（按标题/艺术家/专辑） */
    PageResult<TrackResponse> search(String keyword, Long page, Long size);

    /** 获取曲目的音频文件（用于流式播放） */
    File getAudioFile(Long id);

    /** 获取曲目的预计算波形峰值数据 */
    TrackPeaksResponse getPeaks(Long id, Integer samples);

    /** 上传/更新歌词 */
    void updateLyrics(Long id, String lyrics);

    /** 获取歌词 */
    String getLyrics(Long id);
}
