package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 旅游攻略实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_guide")
public class Guide extends BaseEntity {

    /**
     * 攻略标题
     */
    private String title;

    /**
     * 摘要/副标题
     */
    private String summary;

    /**
     * 正文内容（Markdown格式）
     */
    private String content;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 关联地区ID
     */
    private Long regionId;

    /**
     * 行程天数
     */
    private Integer durationDays;

    /**
     * 最低预算
     */
    private BigDecimal budgetMin;

    /**
     * 最高预算
     */
    private BigDecimal budgetMax;

    /**
     * 适合季节：春/夏/秋/冬/全年
     */
    private String season;

    /**
     * 旅行风格：亲子/情侣/独自/朋友
     */
    private String travelStyle;

    /**
     * 关联景区ID列表（JSON数组）
     */
    private String attractions;

    /**
     * 旅行贴士（JSON格式 [{"title":"...","content":"..."}]）
     */
    private String tips;

    /**
     * 行程结构（JSON格式 [{"day":1,"title":"...","spots":[],"hotel":"...","meals":[]}]）
     */
    private String itinerary;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 作者ID
     */
    private Long authorId;
}
