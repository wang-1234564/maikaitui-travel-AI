-- ============================================================
-- Admin AI Console — 数据库迁移 V2
-- 用途: 评论审核字段 + AI操作日志表
-- 日期: 2026-06-04
-- ============================================================

-- 1. tourism_comment 增加审核字段
ALTER TABLE tourism_comment
    ADD COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态: pending/approved/rejected',
    ADD COLUMN audit_reason VARCHAR(500) COMMENT '审核原因/违规说明',
    ADD COLUMN risk_score INT DEFAULT 0 COMMENT 'AI风险评估分数 0-100',
    ADD INDEX idx_status (status);

-- 已有评论统一设为已通过
UPDATE tourism_comment SET status = 'approved';

-- 2. 管理员 AI 操作日志表
CREATE TABLE IF NOT EXISTS admin_ai_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) COMMENT '操作管理员用户名',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型: comment_query/comment_approve/comment_reject/comment_delete/comment_stats',
    action_desc VARCHAR(500) COMMENT '操作描述',
    target_type VARCHAR(50) COMMENT '目标类型: comment',
    target_id BIGINT COMMENT '目标ID',
    target_snapshot TEXT COMMENT '目标快照(JSON)',
    ai_response TEXT COMMENT 'AI操作响应摘要',
    success TINYINT DEFAULT 1 COMMENT '是否成功 1=成功 0=失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    execute_time_ms BIGINT COMMENT '执行耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_action_type (action_type),
    INDEX idx_target (target_type, target_id),
    INDEX idx_create_time (create_time)
) COMMENT 'Admin AI 操作日志';
