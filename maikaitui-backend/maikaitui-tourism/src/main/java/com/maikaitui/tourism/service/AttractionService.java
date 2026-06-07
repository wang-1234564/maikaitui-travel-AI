package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Attraction;

/**
 * 景点服务接口
 */
public interface AttractionService {

    /**
     * 分页查询景点列表
     */
    Result getAttractionList(int page, int size, Long regionId, Long categoryId, String keyword, String sortBy);

    /**
     * 查询热门景点
     */
    Result getHotAttractions(int limit);

    /**
     * 根据ID查询景点详情
     */
    Result getAttractionById(Long id);

    /**
     * 新增景点
     */
    Result addAttraction(Attraction attraction);

    /**
     * 修改景点
     */
    Result updateAttraction(Attraction attraction);

    /**
     * 删除景点
     */
    Result deleteAttraction(Long id);

    /**
     * 获取推荐景点
     */
    Result getRecommendations(Long attractionId, int limit);

    /**
     * 增加浏览次数
     */
    Result incrementViewCount(Long id);
}
