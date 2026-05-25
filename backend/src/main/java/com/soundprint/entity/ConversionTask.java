package com.soundprint.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 格式转换任务表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("conversion_task")
public class ConversionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发起用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 源曲目 ID
     */
    @TableField("source_track_id")
    private Long sourceTrackId;

    /**
     * 源格式
     */
    @TableField("source_format")
    private String sourceFormat;

    /**
     * 目标格式：FLAC/MP3/WAV/AAC
     */
    @TableField("target_format")
    private String targetFormat;

    /**
     * 目标比特率（kbps，无损时可为 NULL）
     */
    @TableField("target_bitrate")
    private Integer targetBitrate;

    /**
     * 目标采样率（Hz）
     */
    @TableField("target_sample_rate")
    private Integer targetSampleRate;

    /**
     * PENDING/RUNNING/SUCCESS/FAILED
     */
    @TableField("status")
    private String status;

    /**
     * 进度 0-100
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 转换后文件路径
     */
    @TableField("output_path")
    private String outputPath;

    /**
     * 失败时的错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 开始执行时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 完成/失败时间
     */
    @TableField("finished_at")
    private LocalDateTime finishedAt;
}
