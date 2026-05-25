package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.dto.request.tag.TagCreateRequest;
import com.soundprint.dto.request.tag.TagUpdateRequest;
import com.soundprint.dto.response.TagResponse;
import com.soundprint.entity.Tag;

import java.util.List;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface TagService extends IService<Tag> {

    List<TagResponse> listAll();

    TagResponse create(TagCreateRequest request);

    TagResponse updateTag(Long id, TagUpdateRequest request);

    void deleteTag(Long id);

    /** 覆盖式给曲目打标签 */
    void assignTagsToTrack(Long trackId, List<Long> tagIds);
}
