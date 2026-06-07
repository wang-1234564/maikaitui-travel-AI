package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping
    public Result addFavorite(@RequestHeader("X-User-Id") Long userId,
                              @RequestParam Long attractionId) {
        return favoriteService.addFavorite(userId, attractionId);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{attractionId}")
    public Result removeFavorite(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long attractionId) {
        return favoriteService.removeFavorite(userId, attractionId);
    }

    /**
     * 分页查询收藏列表
     */
    @GetMapping("/list")
    public Result getFavorites(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestHeader("X-User-Id") Long userId) {
        return favoriteService.getFavorites(page, size, userId);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{attractionId}")
    public Result isFavorited(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long attractionId) {
        return favoriteService.isFavorited(userId, attractionId);
    }
}
