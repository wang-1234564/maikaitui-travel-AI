package com.maikaitui.tourism.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表（含景点信息）
 */
@Data
public class FavoriteVO {

    private Long id;
    private Long attractionId;
    private LocalDateTime createTime;

    private String name;
    private String coverImage;
    private String images;
    private BigDecimal price;
    private Double rating;
    private Long regionId;
    private String regionName;
}
