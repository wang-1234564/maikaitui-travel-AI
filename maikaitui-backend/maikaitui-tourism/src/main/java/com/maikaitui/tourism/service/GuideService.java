package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Guide;

/**
 * 攻略服务接口
 */
public interface GuideService {

    /**
     * 分页查询攻略列表
     */
    Result getGuideList(int page, int size, String destination, Integer durationDays,
                        String season, String travelStyle, String sortBy);

    /**
     * 查询热门攻略
     */
    Result getHotGuides(int limit);

    /**
     * 根据ID查询攻略详情
     */
    Result getGuideById(Long id);

    /**
     * 查询某景区的相关攻略
     */
    Result getGuidesByAttraction(Long attractionId, int limit);

    /**
     * 新增攻略
     */
    Result addGuide(Guide guide);

    /**
     * 修改攻略
     */
    Result updateGuide(Guide guide);

    /**
     * 删除攻略
     */
    Result deleteGuide(Long id);
}
