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
 * 曲目-标签关联表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("track_tag")
public class TrackTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 曲目 ID
     */
    @TableId(value = "track_id", type = IdType.INPUT)
    private Long trackId;

    /**
     * 标签 ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 打标签时间
     */
    @TableField("tagged_at")
    private LocalDateTime taggedAt;
}
