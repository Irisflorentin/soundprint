package com.soundprint.dto.request.conversion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交转换任务入参
 */
@Data
public class ConversionSubmitRequest {
    @NotNull(message = "源曲目 ID 不能为空")
    private Long sourceTrackId;

    @NotBlank(message = "目标格式不能为空")
    private String targetFormat;   // FLAC/MP3/WAV/AAC

    /** 目标比特率（kbps，无损可不填） */
    private Integer targetBitrate;

    /** 目标采样率（Hz，可不填） */
    private Integer targetSampleRate;
}
