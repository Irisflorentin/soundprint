package com.soundprint.mapper;

import com.soundprint.dto.response.PlayHistoryResponse;
import com.soundprint.entity.PlayHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 播放历史表 Mapper 接口
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Mapper
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    /** 最近播放（按曲目去重，每首歌只保留最近一次） */
    List<PlayHistoryResponse> selectRecentDistinct(@Param("userId") Long userId, @Param("limit") Integer limit);
}
