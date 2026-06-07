package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Guide;
import com.maikaitui.tourism.mapper.GuideMapper;
import com.maikaitui.tourism.service.GuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 攻略服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

    private final GuideMapper guideMapper;

    @Override
    public Result getGuideList(int page, int size, String destination, Integer durationDays,
                               String season, String travelStyle, String sortBy) {
        Page<Guide> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Guide> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的
        wrapper.eq(Guide::getStatus, 1);

        // 目的地筛选
        if (destination != null && !destination.isBlank()) {
            wrapper.like(Guide::getDestination, destination);
        }

        // 天数筛选
        if (durationDays != null) {
            wrapper.eq(Guide::getDurationDays, durationDays);
        }

        // 季节筛选
        if (season != null && !season.isBlank()) {
            wrapper.eq(Guide::getSeason, season);
        }

        // 旅行风格筛选
        if (travelStyle != null && !travelStyle.isBlank()) {
            wrapper.eq(Guide::getTravelStyle, travelStyle);
        }

        // 排序
        if ("popular".equals(sortBy)) {
            wrapper.orderByDesc(Guide::getViewCount);
        } else if ("newest".equals(sortBy)) {
            wrapper.orderByDesc(Guide::getCreateTime);
        } else {
            // 默认：热门优先 + 最新
            wrapper.orderByDesc(Guide::getViewCount)
                   .orderByDesc(Guide::getCreateTime);
        }

        IPage<Guide> result = guideMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getHotGuides(int limit) {
        LambdaQueryWrapper<Guide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Guide::getStatus, 1)
               .orderByDesc(Guide::getViewCount)
               .last("LIMIT " + limit);
        List<Guide> list = guideMapper.selectList(wrapper);
        return Result.success(list);
    }

    @Override
    public Result getGuideById(Long id) {
        Guide guide = guideMapper.selectById(id);
        if (guide == null) {
            return Result.error("攻略不存在");
        }
        // 增加浏览次数
        LambdaUpdateWrapper<Guide> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("view_count = view_count + 1")
                     .eq(Guide::getId, id);
        guideMapper.update(null, updateWrapper);
        // 更新返回数据中的浏览数
        guide.setViewCount(guide.getViewCount() + 1);
        return Result.success(guide);
    }

    @Override
    public Result getGuidesByAttraction(Long attractionId, int limit) {
        // 使用 JSON_CONTAINS 查询关联了该景区的攻略
        LambdaQueryWrapper<Guide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Guide::getStatus, 1)
               .apply("JSON_CONTAINS(attractions, {0})", attractionId)
               .orderByDesc(Guide::getViewCount)
               .last("LIMIT " + limit);
        List<Guide> list = guideMapper.selectList(wrapper);
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result addGuide(Guide guide) {
        if (guide.getViewCount() == null) {
            guide.setViewCount(0L);
        }
        if (guide.getLikeCount() == null) {
            guide.setLikeCount(0L);
        }
        if (guide.getDurationDays() == null) {
            guide.setDurationDays(1);
        }
        if (guide.getStatus() == null) {
            guide.setStatus(1);
        }
        int rows = guideMapper.insert(guide);
        if (rows > 0) {
            log.info("新增攻略成功: {}", guide.getTitle());
            return Result.success("新增攻略成功");
        }
        return Result.error("新增攻略失败");
    }

    @Override
    @Transactional
    public Result updateGuide(Guide guide) {
        if (guide.getId() == null) {
            return Result.error("攻略ID不能为空");
        }
        Guide existing = guideMapper.selectById(guide.getId());
        if (existing == null) {
            return Result.error("攻略不存在");
        }
        int rows = guideMapper.updateById(guide);
        if (rows > 0) {
            log.info("更新攻略成功: {}", guide.getId());
            return Result.success("更新攻略成功");
        }
        return Result.error("更新攻略失败");
    }

    @Override
    @Transactional
    public Result deleteGuide(Long id) {
        int rows = guideMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除攻略成功: {}", id);
            return Result.success("删除攻略成功");
        }
        return Result.error("删除攻略失败");
    }
}
