package com.soundprint.dto.request.playlist;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 歌单重排序入参：按 trackIds 的顺序重新设置 order_index
 */
@Data
public class PlaylistReorderRequest {
    @NotEmpty(message = "排序列表不能为空")
    private List<Long> trackIds;
}
