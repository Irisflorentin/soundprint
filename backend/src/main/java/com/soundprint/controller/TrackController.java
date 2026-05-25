package com.soundprint.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.entity.Track;
import com.soundprint.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<PageResult<Track>> listTracks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size
    ) {
        Page<Track> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Track> wrapper = new LambdaQueryWrapper<Track>()
                .orderByDesc(Track::getCreatedAt);

        Page<Track> result = trackService.page(pageParam, wrapper);

        return Result.success(PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        ));
    }
}
