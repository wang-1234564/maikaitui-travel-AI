package com.maikaitui.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 地区 Mapper — AI 模块本地，读取 tourism_region 表
 */
@Mapper
public interface RegionMapper {

    @Select("SELECT id, name, parent_id, level FROM tourism_region WHERE deleted = 0 AND status = 1 AND level = 3 ORDER BY id")
    List<Map<String, Object>> selectCities();

    @Select("SELECT id, name, parent_id, level FROM tourism_region WHERE deleted = 0 AND status = 1 ORDER BY id")
    List<Map<String, Object>> selectAllRegions();
}
