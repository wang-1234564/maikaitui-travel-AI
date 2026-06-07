package com.maikaitui.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.tourism.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
