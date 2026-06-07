package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tourism/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result getDashboard() {
        return dashboardService.getDashboardData();
    }
}
