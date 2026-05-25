package com.soundprint.dto.request.tag;

import lombok.Data;

import java.util.List;

/**
 * 给曲目打标签入参（覆盖式：用这批 tagIds 覆盖该曲目原有标签）
 */
@Data
public class TrackTagAssignRequest {
    /** 标签 ID 列表，空列表表示清空该曲目的标签 */
    private List<Long> tagIds;
}
