package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.request.artist.ArtistCreateRequest;
import com.soundprint.dto.request.artist.ArtistQueryRequest;
import com.soundprint.dto.request.artist.ArtistUpdateRequest;
import com.soundprint.dto.response.ArtistDetailResponse;
import com.soundprint.dto.response.ArtistResponse;
import com.soundprint.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 艺术家接口
 */
@Tag(name = "艺术家", description = "艺术家 CRUD")
@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @Operation(summary = "分页查询艺术家")
    @GetMapping
    public Result<PageResult<ArtistResponse>> page(ArtistQueryRequest query) {
        return Result.success(artistService.pageQuery(query));
    }

    @Operation(summary = "艺术家详情（含专辑与曲目）")
    @GetMapping("/{id}")
    public Result<ArtistDetailResponse> detail(@PathVariable Long id) {
        return Result.success(artistService.getDetail(id));
    }

    @Operation(summary = "创建艺术家")
    @PostMapping
    public Result<ArtistResponse> create(@RequestBody @Valid ArtistCreateRequest request) {
        return Result.success(artistService.create(request));
    }

    @Operation(summary = "更新艺术家")
    @PutMapping("/{id}")
    public Result<ArtistResponse> update(@PathVariable Long id,
                                         @RequestBody @Valid ArtistUpdateRequest request) {
        return Result.success(artistService.updateArtist(id, request));
    }

    @Operation(summary = "删除艺术家（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return Result.success();
    }
}
