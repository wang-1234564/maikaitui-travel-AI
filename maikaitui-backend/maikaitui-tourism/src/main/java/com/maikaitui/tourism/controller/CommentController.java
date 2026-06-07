package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Comment;
import com.maikaitui.tourism.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询景点评论
     */
    @GetMapping("/list/{attractionId}")
    public Result getCommentsByAttractionId(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @PathVariable Long attractionId) {
        return commentService.getCommentsByAttractionId(page, size, attractionId);
    }

    /**
     * 新增评论
     */
    @PostMapping
    public Result addComment(@Valid @RequestBody Comment comment,
                             @RequestHeader("X-User-Id") Long userId,
                             @RequestHeader(value = "X-User-Name", required = false) String username,
                             @RequestHeader(value = "X-User-Avatar", required = false) String avatar) {
        comment.setUserId(userId);
        if (username != null) {
            comment.setUsername(username);
        }
        if (avatar != null) {
            comment.setAvatar(avatar);
        }
        return commentService.addComment(comment);
    }

    /**
     * 管理端 — 查询全部评论
     */
    @GetMapping("/admin/list")
    public Result getAllComments(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return commentService.getAllComments(page, size);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }

    /**
     * 更新评论审核状态（管理端 / AI 调用）
     */
    @PutMapping("/{id}/status")
    public Result updateCommentStatus(@PathVariable Long id,
                                      @RequestBody java.util.Map<String, Object> body) {
        String status = (String) body.get("status");
        String auditReason = (String) body.get("auditReason");
        Integer riskScore = body.get("riskScore") instanceof Integer
                ? (Integer) body.get("riskScore") : null;
        return commentService.updateStatus(id, status, auditReason, riskScore);
    }
}
