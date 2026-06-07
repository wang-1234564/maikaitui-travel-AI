package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Region;

/**
 * 地区服务接口
 */
public interface RegionService {

    /**
     * 获取地区树
     */
    Result getRegionTree();

    /**
     * 分页查询地区列表
     */
    Result getRegionList(int page, int size);

    /**
     * 根据ID查询地区
     */
    Result getRegionById(Long id);

    /**
     * 新增地区
     */
    Result addRegion(Region region);

    /**
     * 修改地区
     */
    Result updateRegion(Region region);

    /**
     * 删除地区
     */
    Result deleteRegion(Long id);
}
