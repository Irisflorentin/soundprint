package com.soundprint.dto.response;

import com.soundprint.entity.Tag;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签响应 DTO
 */
@Data
public class TagResponse {
    private Long id;
    private String name;
    private String color;

    public static TagResponse from(Tag tag) {
        if (tag == null) return null;
        TagResponse r = new TagResponse();
        r.setId(tag.getId());
        r.setName(tag.getName());
        r.setColor(tag.getColor());
        return r;
    }

    public static List<TagResponse> from(List<Tag> tags) {
        if (tags == null) return Collections.emptyList();
        return tags.stream().map(TagResponse::from).collect(Collectors.toList());
    }
}
