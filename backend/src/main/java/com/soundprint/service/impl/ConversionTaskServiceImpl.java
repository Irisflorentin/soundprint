package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.conversion.ConversionSubmitRequest;
import com.soundprint.dto.response.ConversionTaskResponse;
import com.soundprint.entity.ConversionTask;
import com.soundprint.entity.Track;
import com.soundprint.exception.BusinessException;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.ConversionTaskMapper;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.service.ConversionExecutionService;
import com.soundprint.service.ConversionTaskService;
import com.soundprint.util.CurrentUserUtil;
import com.soundprint.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * <p>
 * 格式转换任务表 服务实现类
 * </p>
 *
 * Phase 5 接入真实 FFmpeg：提交任务后立即返回，后台线程负责转码并更新进度。
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionTaskServiceImpl extends ServiceImpl<ConversionTaskMapper, ConversionTask> implements ConversionTaskService {

    private final TrackMapper trackMapper;
    private final FileStorageUtil fileStorageUtil;
    private final CurrentUserUtil currentUserUtil;
    private final ConversionExecutionService conversionExecutionService;

    @Override
    public ConversionTaskResponse submit(ConversionSubmitRequest request) {
        Track src = trackMapper.selectById(request.getSourceTrackId());
        if (src == null) {
            throw new ResourceNotFoundException("曲目", request.getSourceTrackId());
        }
        ConversionTask task = new ConversionTask();
        task.setUserId(currentUserUtil.getCurrentUserId());
        task.setSourceTrackId(request.getSourceTrackId());
        task.setSourceFormat(src.getFormat());
        task.setTargetFormat(request.getTargetFormat());
        task.setTargetBitrate(request.getTargetBitrate());
        task.setTargetSampleRate(request.getTargetSampleRate());
        task.setStatus("PENDING");
        task.setProgress(0);
        save(task);

        // 后台异步转换（不阻塞当前请求）
        conversionExecutionService.executeConversion(task.getId());

        return ConversionTaskResponse.from(task);
    }

    @Override
    public ConversionTaskResponse getTask(Long id) {
        ConversionTask t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("转换任务", id);
        }
        return ConversionTaskResponse.from(t);
    }

    @Override
    public PageResult<ConversionTaskResponse> pageQuery(Long page, Long size) {
        Page<ConversionTask> p = new Page<>(page, size);
        Page<ConversionTask> result = page(p, new LambdaQueryWrapper<ConversionTask>()
                .eq(ConversionTask::getUserId, currentUserUtil.getCurrentUserId())
                .orderByDesc(ConversionTask::getCreatedAt));
        return PageResult.of(ConversionTaskResponse.from(result.getRecords()),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public File getOutputFile(Long id) {
        ConversionTask t = getById(id);
        if (t == null) {
            throw new ResourceNotFoundException("转换任务", id);
        }
        if (!"SUCCESS".equals(t.getStatus())) {
            throw new BusinessException("任务尚未完成，无法下载");
        }
        if (t.getOutputPath() == null) {
            throw new BusinessException("该任务没有输出文件");
        }
        File f = fileStorageUtil.getFile(t.getOutputPath());
        if (!f.exists()) {
            throw new BusinessException(404, "输出文件不存在: " + t.getOutputPath());
        }
        return f;
    }

}
