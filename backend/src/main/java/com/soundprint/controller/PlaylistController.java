package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.request.playlist.PlaylistCreateRequest;
import com.soundprint.dto.request.playlist.PlaylistReorderRequest;
import com.soundprint.dto.request.playlist.PlaylistTrackAddRequest;
import com.soundprint.dto.request.playlist.PlaylistUpdateRequest;
import com.soundprint.dto.response.PlaylistDetailResponse;
import com.soundprint.dto.response.PlaylistResponse;
import com.soundprint.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 歌单接口
 */
@Tag(name = "歌单", description = "歌单 CRUD + 加歌/删歌/排序")
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "分页查询我的歌单")
    @GetMapping
    public Result<PageResult<PlaylistResponse>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(playlistService.pageQuery(page, size));
    }

    @Operation(summary = "歌单详情（含曲目列表）")
    @GetMapping("/{id}")
    public Result<PlaylistDetailResponse> detail(@PathVariable Long id) {
        return Result.success(playlistService.getDetail(id));
    }

    @Operation(summary = "创建歌单")
    @PostMapping
    public Result<PlaylistResponse> create(@RequestBody @Valid PlaylistCreateRequest request) {
        return Result.success(playlistService.create(request));
    }

    @Operation(summary = "更新歌单")
    @PutMapping("/{id}")
    public Result<PlaylistResponse> update(@PathVariable Long id,
                                           @RequestBody @Valid PlaylistUpdateRequest request) {
        return Result.success(playlistService.updatePlaylist(id, request));
    }

    @Operation(summary = "删除歌单（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return Result.success();
    }

    @Operation(summary = "添加曲目到歌单")
    @PostMapping("/{id}/tracks")
    public Result<Void> addTrack(@PathVariable Long id,
                                 @RequestBody @Valid PlaylistTrackAddRequest request) {
        playlistService.addTrack(id, request.getTrackId());
        return Result.success();
    }

    @Operation(summary = "从歌单移除曲目")
    @DeleteMapping("/{id}/tracks/{trackId}")
    public Result<Void> removeTrack(@PathVariable Long id, @PathVariable Long trackId) {
        playlistService.removeTrack(id, trackId);
        return Result.success();
    }

    @Operation(summary = "歌单内曲目重排序")
    @PutMapping("/{id}/reorder")
    public Result<Void> reorder(@PathVariable Long id,
                                @RequestBody @Valid PlaylistReorderRequest request) {
        playlistService.reorder(id, request);
        return Result.success();
    }
}
