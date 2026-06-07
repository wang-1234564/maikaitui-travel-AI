package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_favorite")
public class Favorite extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 景点ID
     */
    private Long attractionId;
}
