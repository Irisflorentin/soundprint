package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.dto.request.tag.TagCreateRequest;
import com.soundprint.dto.request.tag.TagUpdateRequest;
import com.soundprint.dto.response.TagResponse;
import com.soundprint.entity.Tag;
import com.soundprint.entity.TrackTag;
import com.soundprint.exception.ResourceNotFoundException;
import com.soundprint.mapper.TagMapper;
import com.soundprint.mapper.TrackTagMapper;
import com.soundprint.service.TagService;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TrackTagMapper trackTagMapper;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public List<TagResponse> listAll() {
        List<Tag> tags = list(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, currentUserUtil.getCurrentUserId())
                .orderByAsc(Tag::getId));
        return TagResponse.from(tags);
    }

    @Override
    public TagResponse create(TagCreateRequest request) {
        Tag tag = new Tag();
        tag.setUserId(currentUserUtil.getCurrentUserId());
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        save(tag);
        return TagResponse.from(tag);
    }

    @Override
    public TagResponse updateTag(Long id, TagUpdateRequest request) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("标签", id);
        }
        if (request.getName() != null) tag.setName(request.getName());
        if (request.getColor() != null) tag.setColor(request.getColor());
        updateById(tag);
        return TagResponse.from(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("标签", id);
        }
        // tag 无软删除：物理删除标签 + 清理它在 track_tag 里的关联
        trackTagMapper.delete(new LambdaQueryWrapper<TrackTag>().eq(TrackTag::getTagId, id));
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTagsToTrack(Long trackId, List<Long> tagIds) {
        // 覆盖式：先清空该曲目原有标签，再插入新的
        trackTagMapper.delete(new LambdaQueryWrapper<TrackTag>().eq(TrackTag::getTrackId, trackId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            TrackTag tt = new TrackTag();
            tt.setTrackId(trackId);
            tt.setTagId(tagId);
            tt.setTaggedAt(LocalDateTime.now());
            trackTagMapper.insert(tt);
        }
    }
}
