package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Attraction;
import com.maikaitui.tourism.entity.Comment;
import com.maikaitui.tourism.mapper.AttractionMapper;
import com.maikaitui.tourism.mapper.CommentMapper;
import com.maikaitui.tourism.service.CommentService;
import com.maikaitui.tourism.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final AttractionMapper attractionMapper;

    @Override
    public Result getCommentsByAttractionId(int page, int size, Long attractionId) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getAttractionId, attractionId)
               .orderByDesc(Comment::getCreateTime);
        IPage<Comment> result = commentMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    @Transactional
    public Result addComment(Comment comment) {
        if (comment.getRating() == null || comment.getRating() < 1 || comment.getRating() > 5) {
            comment.setRating(5);
        }
        if (comment.getUsername() == null || comment.getUsername().isBlank()) {
            comment.setUsername("游客");
        }
        comment.setDeleted(0);
        comment.setStatus("pending"); // 新评论默认待审核
        comment.setRiskScore(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        int rows = commentMapper.insert(comment);
        if (rows > 0) {
            log.info("新增评论成功: attractionId={}, userId={}, username={}, rating={}",
                    comment.getAttractionId(), comment.getUserId(), comment.getUsername(), comment.getRating());
            return Result.success("评论成功");
        }
        return Result.error("评论失败");
    }

    @Override
    public Result getAllComments(int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentMapper.selectPage(pageParam, wrapper);

        // 收集所有景点 ID，批量查询景点名称
        Set<Long> attractionIds = commentPage.getRecords().stream()
                .map(Comment::getAttractionId)
                .collect(Collectors.toSet());
        final Map<Long, String> attractionNameMap;
        if (!attractionIds.isEmpty()) {
            List<Attraction> attractions = attractionMapper.selectBatchIds(attractionIds);
            attractionNameMap = attractions.stream()
                    .collect(Collectors.toMap(Attraction::getId, Attraction::getName));
        } else {
            attractionNameMap = Map.of();
        }

        // 组装 CommentVO 列表
        List<CommentVO> voRecords = commentPage.getRecords().stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setAttractionId(c.getAttractionId());
            vo.setAttractionName(attractionNameMap.getOrDefault(c.getAttractionId(), "未知景点"));
            vo.setUserId(c.getUserId());
            vo.setUsername(c.getUsername());
            vo.setAvatar(c.getAvatar());
            vo.setContent(c.getContent());
            vo.setRating(c.getRating());
            vo.setParentId(c.getParentId());
            vo.setImages(c.getImages());
            vo.setStatus(c.getStatus());
            vo.setAuditReason(c.getAuditReason());
            vo.setRiskScore(c.getRiskScore());
            vo.setCreateTime(c.getCreateTime());
            vo.setUpdateTime(c.getUpdateTime());
            return vo;
        }).toList();

        // 构造分页返回
        Page<CommentVO> voPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        voPage.setRecords(voRecords);
        return Result.success(voPage);
    }

    @Override
    @Transactional
    public Result deleteComment(Long id) {
        int rows = commentMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除评论成功: {}", id);
            return Result.success("删除评论成功");
        }
        return Result.error("删除评论失败");
    }

    @Override
    @Transactional
    public Result updateStatus(Long id, String status, String auditReason, Integer riskScore) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setStatus(status);
        if (auditReason != null) {
            comment.setAuditReason(auditReason);
        }
        if (riskScore != null) {
            comment.setRiskScore(riskScore);
        }
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        log.info("评论状态更新: id={}, status={}, reason={}, riskScore={}", id, status, auditReason, riskScore);
        return Result.success("状态更新成功");
    }
}
