package com.maikaitui.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.ai.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Comment Mapper — AI 模块本地，直接查询 tourism_comment 表
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {

    @Select("SELECT id, attraction_id, user_id, username, content, rating, status, risk_score, audit_reason, create_time "
            + "FROM tourism_comment WHERE deleted = 0 AND (status = 'pending' OR status IS NULL) "
            + "ORDER BY create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectPendingComments(@Param("limit") int limit);

    @Select("SELECT id, attraction_id, user_id, username, content, rating, status, risk_score, audit_reason, create_time "
            + "FROM tourism_comment WHERE deleted = 0 AND content LIKE CONCAT('%',#{keyword},'%') "
            + "ORDER BY create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> searchByKeyword(@Param("keyword") String keyword,
                                               @Param("limit") int limit);

    @Update("UPDATE tourism_comment SET status = #{status}, audit_reason = #{reason}, "
            + "risk_score = #{riskScore}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("reason") String reason, @Param("riskScore") Integer riskScore);

    @Select("SELECT "
            + "COUNT(*) as total, "
            + "COALESCE(SUM(CASE WHEN status = 'pending' OR status IS NULL THEN 1 ELSE 0 END), 0) as pending, "
            + "COALESCE(SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END), 0) as approved, "
            + "COALESCE(SUM(CASE WHEN status = 'rejected' THEN 1 ELSE 0 END), 0) as rejected "
            + "FROM tourism_comment WHERE deleted = 0")
    Map<String, Object> getCommentStats();
}
