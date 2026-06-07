package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Region;
import com.maikaitui.tourism.mapper.RegionMapper;
import com.maikaitui.tourism.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地区服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;

    @Override
    public Result getRegionTree() {
        List<Region> allRegions = regionMapper.selectList(null);

        // 获取根节点（parentId = 0）
        List<Region> tree = allRegions.stream()
                .filter(r -> r.getParentId() == null || r.getParentId() == 0)
                .collect(Collectors.toList());

        // 递归构建树
        for (Region root : tree) {
            buildChildren(root, allRegions);
        }

        return Result.success(tree);
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(Region parent, List<Region> allRegions) {
        List<Region> children = allRegions.stream()
                .filter(r -> parent.getId().equals(r.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (Region child : children) {
                buildChildren(child, allRegions);
            }
        } else {
            parent.setChildren(new ArrayList<>());
        }
    }

    @Override
    public Result getRegionList(int page, int size) {
        Page<Region> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Region::getLevel)
               .orderByAsc(Region::getSortOrder);
        IPage<Region> result = regionMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getRegionById(Long id) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            return Result.error("地区不存在");
        }
        return Result.success(region);
    }

    @Override
    @Transactional
    public Result addRegion(Region region) {
        if (region.getStatus() == null) {
            region.setStatus(1);
        }
        if (region.getSortOrder() == null) {
            region.setSortOrder(0);
        }
        int rows = regionMapper.insert(region);
        if (rows > 0) {
            log.info("新增地区成功: {}", region.getName());
            return Result.success("新增地区成功");
        }
        return Result.error("新增地区失败");
    }

    @Override
    @Transactional
    public Result updateRegion(Region region) {
        if (region.getId() == null) {
            return Result.error("地区ID不能为空");
        }
        Region existing = regionMapper.selectById(region.getId());
        if (existing == null) {
            return Result.error("地区不存在");
        }
        int rows = regionMapper.updateById(region);
        if (rows > 0) {
            log.info("更新地区成功: {}", region.getId());
            return Result.success("更新地区成功");
        }
        return Result.error("更新地区失败");
    }

    @Override
    @Transactional
    public Result deleteRegion(Long id) {
        // 检查是否有子地区
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getParentId, id);
        Long childCount = regionMapper.selectCount(wrapper);
        if (childCount > 0) {
            return Result.error("该地区下存在子地区，无法删除");
        }
        int rows = regionMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除地区成功: {}", id);
            return Result.success("删除地区成功");
        }
        return Result.error("删除地区失败");
    }
}
