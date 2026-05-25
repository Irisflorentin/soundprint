package com.soundprint.mapper;

import com.soundprint.entity.PlaylistTrack;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 歌单-曲目关联表 Mapper 接口
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Mapper
public interface PlaylistTrackMapper extends BaseMapper<PlaylistTrack> {

}
