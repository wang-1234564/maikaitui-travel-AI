package com.maikaitui.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景区实体镜像 — AI 模块本地使用，对应 tourism_attraction 表
 */
@Data
@TableName("tourism_attraction")
public class AttractionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long regionId;
    private Long categoryId;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal price;
    private Double rating;
    private String coverImage;
    private String images;
    private Long viewCount;
    private Long likeCount;
    private Integer isHot;
    private String openTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
