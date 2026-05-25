package com.soundprint.dto.response;

import com.soundprint.entity.ConversionTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 转换任务响应 DTO
 */
@Data
public class ConversionTaskResponse {
    private Long id;
    private Long sourceTrackId;
    private String sourceFormat;
    private String targetFormat;
    private Integer targetBitrate;
    private Integer targetSampleRate;
    private String status;       // PENDING/RUNNING/SUCCESS/FAILED
    private Integer progress;    // 0-100
    private String outputPath;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public static ConversionTaskResponse from(ConversionTask t) {
        ConversionTaskResponse r = new ConversionTaskResponse();
        r.setId(t.getId());
        r.setSourceTrackId(t.getSourceTrackId());
        r.setSourceFormat(t.getSourceFormat());
        r.setTargetFormat(t.getTargetFormat());
        r.setTargetBitrate(t.getTargetBitrate());
        r.setTargetSampleRate(t.getTargetSampleRate());
        r.setStatus(t.getStatus());
        r.setProgress(t.getProgress());
        r.setOutputPath(t.getOutputPath());
        r.setErrorMessage(t.getErrorMessage());
        r.setCreatedAt(t.getCreatedAt());
        r.setStartedAt(t.getStartedAt());
        r.setFinishedAt(t.getFinishedAt());
        return r;
    }

    public static List<ConversionTaskResponse> from(List<ConversionTask> tasks) {
        if (tasks == null) return Collections.emptyList();
        return tasks.stream().map(ConversionTaskResponse::from).collect(Collectors.toList());
    }
}
