package com.soundprint.controller;

import com.soundprint.common.Result;
import com.soundprint.dto.request.tag.TagCreateRequest;
import com.soundprint.dto.request.tag.TagUpdateRequest;
import com.soundprint.dto.response.TagResponse;
import com.soundprint.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签接口
 */
@Tag(name = "标签", description = "标签 CRUD")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "查询全部标签")
    @GetMapping
    public Result<List<TagResponse>> list() {
        return Result.success(tagService.listAll());
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagResponse> create(@RequestBody @Valid TagCreateRequest request) {
        return Result.success(tagService.create(request));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagResponse> update(@PathVariable Long id,
                                      @RequestBody @Valid TagUpdateRequest request) {
        return Result.success(tagService.updateTag(id, request));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
