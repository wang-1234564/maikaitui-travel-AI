package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Attraction;
import com.maikaitui.tourism.entity.Favorite;
import com.maikaitui.tourism.entity.Region;
import com.maikaitui.tourism.mapper.AttractionMapper;
import com.maikaitui.tourism.mapper.FavoriteMapper;
import com.maikaitui.tourism.mapper.RegionMapper;
import com.maikaitui.tourism.service.FavoriteService;
import com.maikaitui.tourism.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final AttractionMapper attractionMapper;
    private final RegionMapper regionMapper;

    @Override
    @Transactional
    public Result addFavorite(Long userId, Long attractionId) {
        // 检查是否存在未删除的收藏记录
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getAttractionId, attractionId);
        
        Favorite existingFavorite = favoriteMapper.selectOne(wrapper);
        if (existingFavorite != null) {
            return Result.error("已收藏该景点");
        }
        
        // 检查是否存在已删除的收藏记录（逻辑删除）
        LambdaQueryWrapper<Favorite> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.eq(Favorite::getUserId, userId)
                      .eq(Favorite::getAttractionId, attractionId)
                      .eq(Favorite::getDeleted, 1);
        
        Favorite deletedFavorite = favoriteMapper.selectOne(deletedWrapper);
        if (deletedFavorite != null) {
            // 恢复已删除的收藏记录
            deletedFavorite.setDeleted(0);
            int rows = favoriteMapper.updateById(deletedFavorite);
            if (rows > 0) {
                log.info("恢复收藏成功: userId={}, attractionId={}", userId, attractionId);
                return Result.success("收藏成功");
            }
            return Result.error("收藏失败");
        }
        
        // 创建新的收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setAttractionId(attractionId);
        
        try {
            int rows = favoriteMapper.insert(favorite);
            if (rows > 0) {
                log.info("收藏成功: userId={}, attractionId={}", userId, attractionId);
                return Result.success("收藏成功");
            }
        } catch (DuplicateKeyException e) {
            log.warn("重复收藏: userId={}, attractionId={}", userId, attractionId);
            return Result.error("已收藏该景点");
        }
        
        return Result.error("收藏失败");
    }

    @Override
    @Transactional
    public Result removeFavorite(Long userId, Long attractionId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getAttractionId, attractionId);
        int rows = favoriteMapper.delete(wrapper);
        if (rows > 0) {
            log.info("取消收藏成功: userId={}, attractionId={}", userId, attractionId);
            return Result.success("取消收藏成功");
        }
        return Result.success("取消收藏成功");
    }

    @Override
    public Result getFavorites(int page, int size, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreateTime);
        IPage<Favorite> result = favoriteMapper.selectPage(new Page<>(page, size), wrapper);

        List<FavoriteVO> voList = new ArrayList<>();
        for (Favorite favorite : result.getRecords()) {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(favorite.getId());
            vo.setAttractionId(favorite.getAttractionId());
            vo.setCreateTime(favorite.getCreateTime());
            Attraction attraction = attractionMapper.selectById(favorite.getAttractionId());
            if (attraction != null) {
                vo.setName(attraction.getName());
                vo.setCoverImage(attraction.getCoverImage());
                vo.setImages(attraction.getImages());
                vo.setPrice(attraction.getPrice());
                vo.setRating(attraction.getRating());
                vo.setRegionId(attraction.getRegionId());
                // 填充地区名称
                if (attraction.getRegionId() != null) {
                    Region region = regionMapper.selectById(attraction.getRegionId());
                    if (region != null) {
                        vo.setRegionName(region.getName());
                    }
                }
            }
            voList.add(vo);
        }

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", voList);
        pageData.put("total", result.getTotal());
        pageData.put("current", result.getCurrent());
        pageData.put("size", result.getSize());
        pageData.put("pages", result.getPages());
        return Result.success(pageData);
    }

    @Override
    public Result isFavorited(Long userId, Long attractionId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getAttractionId, attractionId);
        boolean favorited = favoriteMapper.selectCount(wrapper) > 0;
        return Result.success(favorited);
    }
}
