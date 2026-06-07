package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Attraction;
import com.maikaitui.tourism.mapper.AttractionMapper;
import com.maikaitui.tourism.service.AttractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 景点服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionMapper attractionMapper;

    @Override
    public Result getAttractionList(int page, int size, Long regionId, Long categoryId, String keyword, String sortBy) {
        Page<Attraction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的
        wrapper.eq(Attraction::getStatus, 1);

        // 地区筛选
        if (regionId != null) {
            wrapper.eq(Attraction::getRegionId, regionId);
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Attraction::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Attraction::getName, keyword)
                    .or()
                    .like(Attraction::getDescription, keyword));
        }

        // 排序
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Attraction::getIsHot)
                   .orderByDesc(Attraction::getViewCount);
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Attraction::getRating);
        } else if ("price".equals(sortBy)) {
            wrapper.orderByAsc(Attraction::getPrice);
        } else {
            // newest - 默认按创建时间倒序
            wrapper.orderByDesc(Attraction::getCreateTime);
        }

        IPage<Attraction> result = attractionMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getHotAttractions(int limit) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attraction::getIsHot, 1)
               .eq(Attraction::getStatus, 1)
               .orderByDesc(Attraction::getViewCount)
               .last("LIMIT " + limit);
        List<Attraction> list = attractionMapper.selectList(wrapper);
        return Result.success(list);
    }

    @Override
    public Result getAttractionById(Long id) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            return Result.error("景点不存在");
        }
        // 增加浏览次数
        incrementViewCount(id);
        return Result.success(attraction);
    }

    @Override
    @Transactional
    public Result addAttraction(Attraction attraction) {
        if (attraction.getRating() == null) {
            attraction.setRating(5.0);
        }
        if (attraction.getViewCount() == null) {
            attraction.setViewCount(0L);
        }
        if (attraction.getLikeCount() == null) {
            attraction.setLikeCount(0L);
        }
        if (attraction.getIsHot() == null) {
            attraction.setIsHot(0);
        }
        if (attraction.getStatus() == null) {
            attraction.setStatus(1);
        }
        int rows = attractionMapper.insert(attraction);
        if (rows > 0) {
            log.info("新增景点成功: {}", attraction.getName());
            return Result.success("新增景点成功");
        }
        return Result.error("新增景点失败");
    }

    @Override
    @Transactional
    public Result updateAttraction(Attraction attraction) {
        if (attraction.getId() == null) {
            return Result.error("景点ID不能为空");
        }
        Attraction existing = attractionMapper.selectById(attraction.getId());
        if (existing == null) {
            return Result.error("景点不存在");
        }
        int rows = attractionMapper.updateById(attraction);
        if (rows > 0) {
            log.info("更新景点成功: {}", attraction.getId());
            return Result.success("更新景点成功");
        }
        return Result.error("更新景点失败");
    }

    @Override
    @Transactional
    public Result deleteAttraction(Long id) {
        int rows = attractionMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除景点成功: {}", id);
            return Result.success("删除景点成功");
        }
        return Result.error("删除景点失败");
    }

    @Override
    public Result getRecommendations(Long attractionId, int limit) {
        Attraction source = attractionMapper.selectById(attractionId);
        if (source == null) {
            return Result.error("景点不存在");
        }

        // 优先推荐同地区的景点
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attraction::getRegionId, source.getRegionId())
               .ne(Attraction::getId, attractionId)
               .eq(Attraction::getStatus, 1)
               .orderByDesc(Attraction::getRating)
               .last("LIMIT " + limit);

        List<Attraction> recommendations = attractionMapper.selectList(wrapper);

        // 如果同地区不够，补充同分类的推荐
        if (recommendations.size() < limit && source.getCategoryId() != null) {
            LambdaQueryWrapper<Attraction> fillWrapper = new LambdaQueryWrapper<>();
            fillWrapper.eq(Attraction::getCategoryId, source.getCategoryId())
                       .ne(Attraction::getId, attractionId)
                       .ne(Attraction::getRegionId, source.getRegionId())
                       .eq(Attraction::getStatus, 1)
                       .orderByDesc(Attraction::getRating)
                       .last("LIMIT " + (limit - recommendations.size()));

            List<Attraction> fillList = attractionMapper.selectList(fillWrapper);
            recommendations.addAll(fillList);
        }

        return Result.success(recommendations);
    }

    @Override
    public Result incrementViewCount(Long id) {
        LambdaUpdateWrapper<Attraction> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("view_count = view_count + 1")
               .eq(Attraction::getId, id);
        attractionMapper.update(null, wrapper);
        return Result.success();
    }
}
