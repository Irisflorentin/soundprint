package com.soundprint.dto.request.playlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 歌单加歌入参
 */
@Data
public class PlaylistTrackAddRequest {
    @NotNull(message = "曲目 ID 不能为空")
    private Long trackId;
}
