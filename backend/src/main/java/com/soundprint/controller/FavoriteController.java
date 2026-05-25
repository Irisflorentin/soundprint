package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏接口
 */
@Tag(name = "收藏", description = "收藏/取消/列表/检查")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteService favoriteService;

    @Operation(summary = "收藏曲目")
    @PostMapping("/{trackId}")
    public Result<Void> add(@PathVariable Long trackId) {
        favoriteService.addFavorite(trackId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{trackId}")
    public Result<Void> remove(@PathVariable Long trackId) {
        favoriteService.removeFavorite(trackId);
        return Result.success();
    }

    @Operation(summary = "我的收藏（分页）")
    @GetMapping
    public Result<PageResult<TrackResponse>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        return Result.success(favoriteService.listFavorites(page, size));
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{trackId}")
    public Result<Boolean> check(@PathVariable Long trackId) {
        return Result.success(favoriteService.checkFavorite(trackId));
    }
}
