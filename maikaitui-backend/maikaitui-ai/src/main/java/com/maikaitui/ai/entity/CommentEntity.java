package com.maikaitui.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Comment 实体镜像 — AI 模块本地使用，对应 tourism_comment 表
 * 避免跨模块 Maven 依赖
 */
@Data
@TableName("tourism_comment")
public class CommentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long attractionId;
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
    private Integer deleted;
}
