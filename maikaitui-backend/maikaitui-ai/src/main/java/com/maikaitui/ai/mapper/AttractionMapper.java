package com.maikaitui.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.ai.entity.AttractionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 景区 Mapper — AI 模块本地，读/写 tourism_attraction 表
 */
@Mapper
public interface AttractionMapper extends BaseMapper<AttractionEntity> {

    @Select("SELECT id, name FROM tourism_attraction WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectRecentAttractions(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM tourism_attraction WHERE deleted = 0")
    long countAttractions();
}
