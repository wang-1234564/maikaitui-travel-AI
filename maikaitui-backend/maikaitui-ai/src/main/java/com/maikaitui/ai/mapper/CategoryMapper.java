package com.maikaitui.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 分类 Mapper — AI 模块本地，读取 tourism_category 表
 */
@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name FROM tourism_category WHERE deleted = 0 AND status = 1 AND parent_id = 0 ORDER BY id")
    List<Map<String, Object>> selectTopCategories();
}
