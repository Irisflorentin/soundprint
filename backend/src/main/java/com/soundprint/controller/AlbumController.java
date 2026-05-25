package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.request.album.AlbumCreateRequest;
import com.soundprint.dto.request.album.AlbumQueryRequest;
import com.soundprint.dto.request.album.AlbumUpdateRequest;
import com.soundprint.dto.response.AlbumDetailResponse;
import com.soundprint.dto.response.AlbumResponse;
import com.soundprint.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 专辑接口
 */
@Tag(name = "专辑", description = "专辑 CRUD")
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "分页查询专辑")
    @GetMapping
    public Result<PageResult<AlbumResponse>> page(AlbumQueryRequest query) {
        return Result.success(albumService.pageQuery(query));
    }

    @Operation(summary = "专辑详情（含曲目列表）")
    @GetMapping("/{id}")
    public Result<AlbumDetailResponse> detail(@PathVariable Long id) {
        return Result.success(albumService.getDetail(id));
    }

    @Operation(summary = "创建专辑")
    @PostMapping
    public Result<AlbumResponse> create(@RequestBody @Valid AlbumCreateRequest request) {
        return Result.success(albumService.create(request));
    }

    @Operation(summary = "更新专辑")
    @PutMapping("/{id}")
    public Result<AlbumResponse> update(@PathVariable Long id,
                                        @RequestBody @Valid AlbumUpdateRequest request) {
        return Result.success(albumService.updateAlbum(id, request));
    }

    @Operation(summary = "删除专辑（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return Result.success();
    }
}
