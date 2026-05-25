package com.soundprint.dto.request.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建歌单入参
 */
@Data
public class PlaylistCreateRequest {
    @NotBlank(message = "歌单名不能为空")
    @Size(max = 200, message = "歌单名最长 200 字")
    private String name;

    private String description;

    private String coverUrl;
}
