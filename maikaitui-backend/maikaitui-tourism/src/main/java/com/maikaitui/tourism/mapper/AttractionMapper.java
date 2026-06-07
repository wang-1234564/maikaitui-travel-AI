package com.maikaitui.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.tourism.entity.Attraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 景点 Mapper
 */
@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {

    /**
     * 增加浏览次数
     */
    @Update("UPDATE tourism_attraction SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
}
