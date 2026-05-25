package com.soundprint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.Track;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 曲目表 Mapper 接口
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Mapper
public interface TrackMapper extends BaseMapper<Track> {

    /**
     * 分页查询曲目，LEFT JOIN 艺术家/专辑，一次查询把关联名字带回来，避免 N+1。
     * IPage 参数被 MyBatis-Plus 分页插件识别，自动补 LIMIT 和 COUNT。
     */
    IPage<TrackResponse> pageWithRelations(IPage<TrackResponse> page,
                                           @Param("keyword") String keyword,
                                           @Param("artistId") Long artistId,
                                           @Param("albumId") Long albumId,
                                           @Param("format") String format);

    /** 某专辑下所有曲目（按 track_number 排序） */
    List<TrackResponse> listByAlbum(@Param("albumId") Long albumId);

    /** 某艺术家的所有曲目 */
    List<TrackResponse> listByArtist(@Param("artistId") Long artistId);

    /** 某歌单内的曲目（按 order_index 排序） */
    List<TrackResponse> listByPlaylist(@Param("playlistId") Long playlistId);

    /** 某用户收藏的曲目（分页） */
    IPage<TrackResponse> pageByFavorite(IPage<TrackResponse> page, @Param("userId") Long userId);
}
