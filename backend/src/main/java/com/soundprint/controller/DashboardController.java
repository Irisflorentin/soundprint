package com.soundprint.controller;

import com.soundprint.common.Result;
import com.soundprint.dto.response.DashboardResponse;
import com.soundprint.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页聚合接口
 */
@Tag(name = "首页", description = "首页聚合数据")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取首页聚合数据")
    @GetMapping
    public Result<DashboardResponse> get() {
        return Result.success(dashboardService.getDashboard());
    }
}
