package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_comment")
public class Comment extends BaseEntity {

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;

    /**
     * 评论图片集（JSON数组格式）
     */
    private String images;

    /**
     * 审核状态: pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String status;

    /**
     * 审核原因/违规说明
     */
    private String auditReason;

    /**
     * AI风险评估分数 0-100
     */
    private Integer riskScore;
}
