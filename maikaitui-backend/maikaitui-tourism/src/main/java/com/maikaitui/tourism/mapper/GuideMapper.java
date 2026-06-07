package com.maikaitui.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.tourism.entity.Guide;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 攻略 Mapper
 */
@Mapper
public interface GuideMapper extends BaseMapper<Guide> {

    /**
     * 增加浏览次数
     */
    @Update("UPDATE tourism_guide SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞次数
     */
    @Update("UPDATE tourism_guide SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
}
