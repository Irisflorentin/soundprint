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
 * 艺术家表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("artist")
public class Artist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 艺术家名称
     */
    @TableField("name")
    private String name;

    /**
     * 艺术家简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 艺术家头像 URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 国籍/地区
     */
    @TableField("country")
    private String country;

    /**
     * 成立/出道年份
     */
    @TableField("formed_year")
    private Integer formedYear;

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
