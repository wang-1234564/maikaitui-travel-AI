package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 景点实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_attraction")
public class Attraction extends BaseEntity {

    /**
     * 景点名称
     */
    private String name;

    /**
     * 景点描述
     */
    private String description;

    /**
     * 所属地区ID
     */
    private Long regionId;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 门票价格
     */
    private BigDecimal price;

    /**
     * 评分（默认5.0）
     */
    private Double rating;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 图片集（JSON数组格式）
     */
    private String images;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 是否热门：0-否，1-是
     */
    private Integer isHot;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;
}
