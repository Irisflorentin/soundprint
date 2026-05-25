package com.soundprint.service;

import com.soundprint.entity.ConversionTask;
import com.soundprint.entity.Track;
import com.soundprint.mapper.ConversionTaskMapper;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.util.FFmpegRunner;
import com.soundprint.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 转换任务后台执行器。
 *
 * 单独拆成服务是为了让 @Async 通过 Spring 代理生效：
 * Controller 提交任务后立即返回，真正的 FFmpeg 转换在后台线程执行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionExecutionService {

    private final ConversionTaskMapper conversionTaskMapper;
    private final TrackMapper trackMapper;
    private final FileStorageUtil fileStorageUtil;
    private final FFmpegRunner ffmpegRunner;

    @Async
    public void executeConversion(Long taskId) {
        ConversionTask task = conversionTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        try {
            task.setStatus("RUNNING");
            task.setStartedAt(LocalDateTime.now());
            conversionTaskMapper.updateById(task);

            Track sourceTrack = trackMapper.selectById(task.getSourceTrackId());
            if (sourceTrack == null || sourceTrack.getFilePath() == null) {
                throw new IllegalStateException("源曲目不存在或没有音频文件");
            }

            String inputPath = fileStorageUtil.getAbsolutePath(sourceTrack.getFilePath());
            String outputRelative = "conversion/" + UUID.randomUUID() + "."
                    + task.getTargetFormat().toLowerCase();
            String outputPath = fileStorageUtil.getAbsolutePath(outputRelative);
            Path parent = Path.of(outputPath).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            ffmpegRunner.convert(
                    inputPath,
                    outputPath,
                    task.getTargetFormat(),
                    task.getTargetBitrate(),
                    task.getTargetSampleRate(),
                    percent -> {
                        if (percent % 5 == 0 || percent >= 99) {
                            conversionTaskMapper.updateProgress(taskId, percent);
                        }
                    }
            );

            task.setStatus("SUCCESS");
            task.setProgress(100);
            task.setOutputPath(outputRelative);
            task.setFinishedAt(LocalDateTime.now());
            conversionTaskMapper.updateById(task);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("转换失败 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            conversionTaskMapper.updateById(task);
        }
    }
}
