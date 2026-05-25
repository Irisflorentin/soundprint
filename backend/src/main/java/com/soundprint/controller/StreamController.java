package com.soundprint.controller;

import com.soundprint.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 音频流接口
 *
 * 核心：HTTP Range Request。浏览器 <audio> 标签会自动发 Range 头，
 * 服务端只回传请求的字节区间（206 Partial Content），实现边下边播、拖动进度即时响应。
 */
@Tag(name = "音频流", description = "流式播放（HTTP Range Request）")
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private static final long CHUNK_SIZE = 1024 * 1024L;  // 每块 1MB

    private final TrackService trackService;

    @Operation(summary = "流式播放曲目")
    @GetMapping("/{trackId}")
    public ResponseEntity<ResourceRegion> stream(
            @PathVariable Long trackId,
            @RequestHeader HttpHeaders headers) throws IOException {

        File audioFile = trackService.getAudioFile(trackId);
        FileSystemResource resource = new FileSystemResource(audioFile);
        long contentLength = resource.contentLength();

        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;
        HttpStatus status;

        if (ranges.isEmpty()) {
            // 没有 Range 头：返回开头一块，避免一次性把整个大文件灌给浏览器
            long rangeLength = Math.min(CHUNK_SIZE, contentLength);
            region = new ResourceRegion(resource, 0, rangeLength);
            status = HttpStatus.OK;
        } else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);
            region = new ResourceRegion(resource, start, rangeLength);
            status = HttpStatus.PARTIAL_CONTENT;   // 206
        }

        return ResponseEntity.status(status)
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
    }
}
