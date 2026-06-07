package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.service.MiniappService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序聚合接口
 */
@RestController
@RequestMapping("/api/tourism/miniapp")
@RequiredArgsConstructor
public class MiniappController {

    private final MiniappService miniappService;

    /**
     * 首页数据：轮播、分类、热门景点
     */
    @GetMapping("/home")
    public Result home() {
        return miniappService.getHomeData();
    }

    /**
     * 用户统计（收藏/订单/评论数）
     */
    @GetMapping("/user/stats")
    public Result userStats(@RequestHeader("X-User-Id") Long userId) {
        return miniappService.getUserStats(userId);
    }

    /**
     * 偏好推荐景点
     */
    @GetMapping("/recommend")
    public Result recommend(@RequestParam(required = false) String preference,
                            @RequestParam(defaultValue = "6") int limit) {
        return miniappService.recommend(preference, limit);
    }
}
