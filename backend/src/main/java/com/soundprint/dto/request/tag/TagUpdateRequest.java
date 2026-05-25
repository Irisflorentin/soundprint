package com.soundprint.dto.request.tag;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签入参（字段可选）
 */
@Data
public class TagUpdateRequest {
    @Size(max = 50, message = "标签名最长 50 字")
    private String name;

    @Size(max = 20)
    private String color;
}
