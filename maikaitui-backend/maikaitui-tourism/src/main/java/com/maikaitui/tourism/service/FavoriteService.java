package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    Result addFavorite(Long userId, Long attractionId);

    /**
     * 取消收藏
     */
    Result removeFavorite(Long userId, Long attractionId);

    /**
     * 分页查询收藏列表
     */
    Result getFavorites(int page, int size, Long userId);

    /**
     * 是否已收藏
     */
    Result isFavorited(Long userId, Long attractionId);
}
