package com.soundprint.dto.request.album;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新专辑入参（字段可选）
 */
@Data
public class AlbumUpdateRequest {
    @Size(max = 200, message = "专辑名最长 200 字")
    private String title;

    private Long artistId;

    private String coverUrl;

    @Min(value = 1900, message = "发行年份不合理")
    @Max(value = 2100, message = "发行年份不合理")
    private Integer releaseYear;

    @Size(max = 50)
    private String genre;

    private String description;
}
