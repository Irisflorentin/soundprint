package com.soundprint.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.dto.request.playhistory.PlayHistoryRecordRequest;
import com.soundprint.dto.response.PlayHistoryResponse;
import com.soundprint.entity.PlayHistory;
import com.soundprint.mapper.PlayHistoryMapper;
import com.soundprint.service.PlayHistoryService;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 播放历史表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class PlayHistoryServiceImpl extends ServiceImpl<PlayHistoryMapper, PlayHistory> implements PlayHistoryService {

    private final CurrentUserUtil currentUserUtil;

    @Override
    public void record(PlayHistoryRecordRequest request) {
        PlayHistory ph = new PlayHistory();
        ph.setUserId(currentUserUtil.getCurrentUserId());
        ph.setTrackId(request.getTrackId());
        ph.setPlayedSeconds(request.getPlayedSeconds());
        ph.setPlayedAt(LocalDateTime.now());
        baseMapper.insert(ph);
    }

    @Override
    public List<PlayHistoryResponse> recent(Integer limit) {
        return baseMapper.selectRecentDistinct(currentUserUtil.getCurrentUserId(), limit);
    }
}
