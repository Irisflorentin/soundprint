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
import com.soundprint.service.ConversionTaskService;
import com.soundprint.util.CurrentUserUtil;
import com.soundprint.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 格式转换任务表 服务实现类
 * </p>
 *
 * Phase 3 不接 FFmpeg：提交后用后台线程模拟进度 0→100，完成时把源音频
 * 复制一份当作"转换结果"（源是种子占位时写一个占位成品），保证前端能完整
 * 联调"提交→轮询进度→下载"流程。Phase 5 会把 simulate() 换成真正的
 * ProcessBuilder 调 FFmpeg 转码。
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
        task.setStatus("RUNNING");
        task.setProgress(0);
        task.setStartedAt(LocalDateTime.now());
        save(task);

        // 后台异步模拟转换（不阻塞当前请求）
        final Long taskId = task.getId();
        CompletableFuture.runAsync(() -> simulate(taskId, request.getSourceTrackId(), request.getTargetFormat()));

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

    // ====== 私有：模拟转换过程 ======

    private void simulate(Long taskId, Long sourceTrackId, String targetFormat) {
        try {
            for (int pct : new int[]{20, 40, 60, 80}) {
                Thread.sleep(700);
                updateProgress(taskId, pct);
            }

            String ext = targetFormat.toLowerCase();
            Track src = trackMapper.selectById(sourceTrackId);
            File srcFile = (src != null && src.getFilePath() != null)
                    ? fileStorageUtil.getFile(src.getFilePath()) : null;

            String outputPath;
            if (srcFile != null && srcFile.exists()) {
                // 真实文件：复制一份作为"转换结果"
                outputPath = fileStorageUtil.copyFile(srcFile, "conversion", ext);
            } else {
                // 源是种子占位、无真实音频：写一个占位成品，保证下载流程可联调
                byte[] placeholder = ("Soundprint Phase 3 转换占位文件\n"
                        + "源曲目无真实音频（种子占位），Phase 5 接入 FFmpeg 后此处为真实转码结果。\n")
                        .getBytes(StandardCharsets.UTF_8);
                outputPath = fileStorageUtil.storeBytes(placeholder, "conversion", ext);
            }

            ConversionTask t = getById(taskId);
            if (t != null) {
                t.setStatus("SUCCESS");
                t.setProgress(100);
                t.setOutputPath(outputPath);
                t.setFinishedAt(LocalDateTime.now());
                updateById(t);
            }
        } catch (Exception e) {
            log.warn("模拟转换失败: taskId={}, err={}", taskId, e.getMessage());
            ConversionTask t = getById(taskId);
            if (t != null) {
                t.setStatus("FAILED");
                t.setErrorMessage(e.getMessage());
                t.setFinishedAt(LocalDateTime.now());
                updateById(t);
            }
        }
    }

    private void updateProgress(Long taskId, int pct) {
        ConversionTask t = getById(taskId);
        if (t != null) {
            t.setProgress(pct);
            updateById(t);
        }
    }
}
