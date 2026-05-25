package com.soundprint.controller;

import com.soundprint.common.Result;
import com.soundprint.dto.request.playhistory.PlayHistoryRecordRequest;
import com.soundprint.dto.response.PlayHistoryResponse;
import com.soundprint.service.PlayHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 播放历史接口
 */
@Tag(name = "播放历史", description = "记录播放 + 最近播放")
@RestController
@RequestMapping("/api/play-history")
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    @Operation(summary = "记录一次播放")
    @PostMapping
    public Result<Void> record(@RequestBody @Valid PlayHistoryRecordRequest request) {
        playHistoryService.record(request);
        return Result.success();
    }

    @Operation(summary = "最近播放（去重）")
    @GetMapping("/recent")
    public Result<List<PlayHistoryResponse>> recent(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(playHistoryService.recent(limit));
    }
}
