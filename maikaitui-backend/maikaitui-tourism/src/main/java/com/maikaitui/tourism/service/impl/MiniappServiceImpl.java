package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Category;
import com.maikaitui.tourism.mapper.CommentMapper;
import com.maikaitui.tourism.mapper.FavoriteMapper;
import com.maikaitui.tourism.mapper.OrderMapper;
import com.maikaitui.tourism.entity.Comment;
import com.maikaitui.tourism.entity.Favorite;
import com.maikaitui.tourism.entity.Order;
import com.maikaitui.tourism.service.AttractionService;
import com.maikaitui.tourism.service.CategoryService;
import com.maikaitui.tourism.service.MiniappService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MiniappServiceImpl implements MiniappService {

    private final AttractionService attractionService;
    private final CategoryService categoryService;
    private final FavoriteMapper favoriteMapper;
    private final OrderMapper orderMapper;
    private final CommentMapper commentMapper;

    @Override
    public Result getHomeData() {
        Result hotResult = attractionService.getHotAttractions(6);
        Result categoryResult = categoryService.getCategoryTree();

        List<Map<String, Object>> banners = List.of(
                banner("探索世界，迈开腿", "发现旅行灵感，开启精彩旅程", "linear-gradient(135deg, #667EEA, #764BA2)"),
                banner("热门景点推荐", "为你精选最值得去的旅行目的地", "linear-gradient(135deg, #FF6B35, #FFB563)"),
                banner("智能行程规划", "AI 助手帮你规划完美旅行路线", "linear-gradient(135deg, #FA709A, #FEE140)"),
                banner("轻松预订门票", "热门景点优惠，即刻出发", "linear-gradient(135deg, #00B4DB, #0083B0)")
        );

        Map<String, Object> data = new HashMap<>();
        data.put("banners", banners);
        data.put("hotAttractions", hotResult.getData());
        data.put("categories", flattenTopCategories(categoryResult.getData()));
        return Result.success(data);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> flattenTopCategories(Object categoryData) {
        if (!(categoryData instanceof List<?> roots)) {
            return List.of();
        }
        return roots.stream()
                .filter(Category.class::isInstance)
                .map(Category.class::cast)
                .filter(c -> c.getStatus() == null || c.getStatus() == 1)
                .limit(8)
                .map(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("name", c.getName());
                    item.put("icon", c.getIcon() != null ? c.getIcon() : defaultIcon(c.getName()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String defaultIcon(String name) {
        if (name == null) return "🏞️";
        if (name.contains("自然") || name.contains("风光")) return "🏞️";
        if (name.contains("历史") || name.contains("古迹")) return "🏛️";
        if (name.contains("主题")) return "🎢";
        if (name.contains("博物")) return "🖼️";
        if (name.contains("美食")) return "🍜";
        return "✨";
    }

    private Map<String, Object> banner(String title, String subtitle, String color) {
        Map<String, Object> b = new HashMap<>();
        b.put("title", title);
        b.put("subtitle", subtitle);
        b.put("bgColor", color);
        return b;
    }

    @Override
    public Result getUserStats(Long userId) {
        long favorites = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        long orders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
        long comments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("favorites", favorites);
        stats.put("orders", orders);
        stats.put("comments", comments);
        return Result.success(stats);
    }

    @Override
    public Result recommend(String preference, int limit) {
        String keyword = preference != null && !preference.isBlank() ? preference : "热门";
        return attractionService.getAttractionList(1, Math.min(limit, 10), null, null, keyword, "hot");
    }
}
