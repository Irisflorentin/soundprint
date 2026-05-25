package com.soundprint.controller;

import com.soundprint.common.PageResult;
import com.soundprint.common.Result;
import com.soundprint.dto.request.conversion.ConversionSubmitRequest;
import com.soundprint.dto.response.ConversionTaskResponse;
import com.soundprint.service.ConversionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 格式转换接口（Phase 3 骨架，FFmpeg 实装见 Phase 5）
 */
@Tag(name = "转换工坊", description = "音频格式转换任务")
@RestController
@RequestMapping("/api/conversions")
@RequiredArgsConstructor
public class ConversionController {

    private final ConversionTaskService conversionTaskService;

    @Operation(summary = "提交转换任务")
    @PostMapping
    public Result<ConversionTaskResponse> submit(@RequestBody @Valid ConversionSubmitRequest request) {
        return Result.success(conversionTaskService.submit(request));
    }

    @Operation(summary = "查询任务状态/进度")
    @GetMapping("/{id}")
    public Result<ConversionTaskResponse> get(@PathVariable Long id) {
        return Result.success(conversionTaskService.getTask(id));
    }

    @Operation(summary = "我的转换历史（分页）")
    @GetMapping
    public Result<PageResult<ConversionTaskResponse>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(conversionTaskService.pageQuery(page, size));
    }

    @Operation(summary = "下载转换结果（仅 SUCCESS 可下载）")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        File file = conversionTaskService.getOutputFile(id);
        Resource resource = new FileSystemResource(file);
        String filename = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }
}
