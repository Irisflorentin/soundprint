package com.soundprint.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 曲目表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("track")
public class Track implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 曲目标题
     */
    @TableField("title")
    private String title;

    /**
     * 艺术家 ID
     */
    @TableField("artist_id")
    private Long artistId;

    /**
     * 专辑 ID
     */
    @TableField("album_id")
    private Long albumId;

    /**
     * 在专辑中的曲目编号
     */
    @TableField("track_number")
    private Integer trackNumber;

    /**
     * 音频文件磁盘路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 曲目封面（覆盖专辑封面）
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 格式：FLAC/MP3/WAV/AAC/OGG
     */
    @TableField("format")
    private String format;

    /**
     * 时长（秒）
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /**
     * 比特率（kbps）
     */
    @TableField("bitrate_kbps")
    private Integer bitrateKbps;

    /**
     * 采样率（Hz）
     */
    @TableField("sample_rate_hz")
    private Integer sampleRateHz;

    /**
     * 声道数（1=单声道，2=立体声）
     */
    @TableField("channels")
    private Byte channels;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    /**
     * 歌词（LRC 或纯文本）
     */
    @TableField("lyrics")
    private String lyrics;

    /**
     * 扩展元数据（JSON）
     */
    @TableField("extra_metadata")
    private String extraMetadata;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 上传时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
