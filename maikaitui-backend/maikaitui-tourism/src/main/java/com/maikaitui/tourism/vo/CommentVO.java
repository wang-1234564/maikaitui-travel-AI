package com.maikaitui.tourism.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 VO — 管理端用，含景点名称
 */
@Data
public class CommentVO {

    private Long id;
    private Long attractionId;
    private String attractionName;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Integer rating;
    private Long parentId;
    private String images;
    private String status;
    private String auditReason;
    private Integer riskScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
