package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.dto.request.playhistory.PlayHistoryRecordRequest;
import com.soundprint.dto.response.PlayHistoryResponse;
import com.soundprint.entity.PlayHistory;

import java.util.List;

/**
 * <p>
 * 播放历史表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface PlayHistoryService extends IService<PlayHistory> {

    /** 记录一次播放 */
    void record(PlayHistoryRecordRequest request);

    /** 最近播放（去重） */
    List<PlayHistoryResponse> recent(Integer limit);
}
