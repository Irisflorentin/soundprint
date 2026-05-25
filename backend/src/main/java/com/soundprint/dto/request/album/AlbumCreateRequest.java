package com.soundprint.dto.request.album;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建专辑入参
 */
@Data
public class AlbumCreateRequest {
    @NotBlank(message = "专辑名不能为空")
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
