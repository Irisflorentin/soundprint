package com.soundprint.dto.request.track;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 曲目元数据更新入参
 * 所有字段可选：传了才更新，没传保持原值（由 Service 处理）。
 */
@Data
public class TrackUpdateRequest {
    @Size(max = 200, message = "曲目标题最长 200 字")
    private String title;

    private Long artistId;

    private Long albumId;

    private Integer trackNumber;

    private String lyrics;
}
