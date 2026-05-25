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
 * 专辑表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("album")
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专辑名称
     */
    @TableField("title")
    private String title;

    /**
     * 所属艺术家 ID
     */
    @TableField("artist_id")
    private Long artistId;

    /**
     * 专辑封面 URL
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 发行年份
     */
    @TableField("release_year")
    private Integer releaseYear;

    /**
     * 流派（Rock/Pop/Jazz/Classical 等）
     */
    @TableField("genre")
    private String genre;

    /**
     * 专辑介绍
     */
    @TableField("description")
    private String description;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
