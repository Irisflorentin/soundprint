package com.soundprint.entity;

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
 * 播放历史表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("play_history")
public class PlayHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 曲目 ID（不加 FK，允许引用软删除曲目）
     */
    @TableField("track_id")
    private Long trackId;

    /**
     * 实际播放秒数
     */
    @TableField("played_seconds")
    private Integer playedSeconds;

    /**
     * 播放时间
     */
    @TableField("played_at")
    private LocalDateTime playedAt;
}
