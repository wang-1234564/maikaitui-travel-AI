package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Comment;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 分页查询景点评论（用户端）
     */
    Result getCommentsByAttractionId(int page, int size, Long attractionId);

    /**
     * 管理端 — 查询所有评论（可选景点名称、关键字筛选）
     */
    Result getAllComments(int page, int size);

    /**
     * 新增评论
     */
    Result addComment(Comment comment);

    /**
     * 删除评论
     */
    Result deleteComment(Long id);

    /**
     * 更新评论审核状态（管理端 / AI 调用）
     */
    Result updateStatus(Long id, String status, String auditReason, Integer riskScore);
}
