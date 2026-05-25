package com.soundprint.dto.request.playlist;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新歌单入参（字段可选）
 */
@Data
public class PlaylistUpdateRequest {
    @Size(max = 200, message = "歌单名最长 200 字")
    private String name;

    private String description;

    private String coverUrl;
}
