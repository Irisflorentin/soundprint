package com.soundprint.dto.request.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建标签入参
 */
@Data
public class TagCreateRequest {
    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名最长 50 字")
    private String name;

    /** 展示颜色 hex，如 #7C3AED */
    @Size(max = 20)
    private String color;
}
