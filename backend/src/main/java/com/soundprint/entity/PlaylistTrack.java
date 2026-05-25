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
 * 歌单-曲目关联表
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Getter
@Setter
@TableName("playlist_track")
public class PlaylistTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 歌单 ID
     */
    @TableId(value = "playlist_id", type = IdType.INPUT)
    private Long playlistId;

    /**
     * 曲目 ID
     */
    @TableField("track_id")
    private Long trackId;

    /**
     * 在歌单内的排序（小的在前）
     */
    @TableField("order_index")
    private Integer orderIndex;

    /**
     * 加入时间
     */
    @TableField("added_at")
    private LocalDateTime addedAt;
}
