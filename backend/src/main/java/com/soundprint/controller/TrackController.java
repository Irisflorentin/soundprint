package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.request.track.TrackQueryRequest;
import com.soundprint.dto.request.track.TrackUpdateRequest;
import com.soundprint.dto.response.TrackDetailResponse;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 曲目接口
 */
@Tag(name = "曲目", description = "音乐曲目相关接口")
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @Operation(summary = "分页查询曲目")
    @GetMapping
    public Result<PageResult<TrackResponse>> page(TrackQueryRequest query) {
        return Result.success(trackService.pageQuery(query));
    }

    @Operation(summary = "模糊搜索曲目")
    @GetMapping("/search")
    public Result<PageResult<TrackResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(trackService.search(keyword, page, size));
    }

    @Operation(summary = "曲目详情")
    @GetMapping("/{id}")
    public Result<TrackDetailResponse> detail(@PathVariable Long id) {
        return Result.success(trackService.getDetail(id));
    }

    @Operation(summary = "上传音频文件")
    @PostMapping("/upload")
    public Result<TrackResponse> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(trackService.upload(file));
    }

    @Operation(summary = "更新曲目元数据")
    @PutMapping("/{id}")
    public Result<TrackResponse> update(@PathVariable Long id,
                                        @RequestBody @Valid TrackUpdateRequest request) {
        return Result.success(trackService.updateMetadata(id, request));
    }

    @Operation(summary = "删除曲目（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return Result.success();
    }

    @Operation(summary = "更新歌词")
    @PutMapping("/{id}/lyrics")
    public Result<Void> updateLyrics(@PathVariable Long id, @RequestBody String lyrics) {
        trackService.updateLyrics(id, lyrics);
        return Result.success();
    }

    @Operation(summary = "获取歌词")
    @GetMapping("/{id}/lyrics")
    public Result<String> getLyrics(@PathVariable Long id) {
        return Result.success(trackService.getLyrics(id));
    }
}
