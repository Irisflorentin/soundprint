package com.soundprint.mapper;

import com.soundprint.entity.Playlist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 歌单表 Mapper 接口
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {

}
