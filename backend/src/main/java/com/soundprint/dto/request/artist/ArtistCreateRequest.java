package com.soundprint.dto.request.artist;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建艺术家入参
 */
@Data
public class ArtistCreateRequest {
    @NotBlank(message = "艺术家名不能为空")
    @Size(max = 200, message = "艺术家名最长 200 字")
    private String name;

    private String bio;

    private String avatarUrl;

    @Size(max = 50)
    private String country;

    @Min(value = 1900, message = "年份不合理")
    @Max(value = 2100, message = "年份不合理")
    private Integer formedYear;
}
