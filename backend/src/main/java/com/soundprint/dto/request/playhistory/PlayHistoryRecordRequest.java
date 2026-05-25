package com.soundprint.dto.request.playhistory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 记录一次播放入参
 */
@Data
public class PlayHistoryRecordRequest {
    @NotNull(message = "曲目 ID 不能为空")
    private Long trackId;

    /** 实际播放秒数 */
    private Integer playedSeconds;
}
