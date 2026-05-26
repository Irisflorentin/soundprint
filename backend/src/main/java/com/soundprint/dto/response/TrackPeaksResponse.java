package com.soundprint.dto.response;

import lombok.Data;

/**
 * 曲目波形峰值数据。
 */
@Data
public class TrackPeaksResponse {
    /** 实际返回的峰值点数量 */
    private Integer sampleCount;
    /** 曲目时长（秒） */
    private Integer duration;
    /** 单声道峰值，范围通常为 [0, 1] */
    private float[] peaks;
}
