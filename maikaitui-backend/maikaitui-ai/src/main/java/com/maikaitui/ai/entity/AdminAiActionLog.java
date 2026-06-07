package com.maikaitui.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员 AI 操作日志实体
 */
@Data
@TableName("admin_ai_action_log")
public class AdminAiActionLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String actionType;
    private String actionDesc;
    private String targetType;
    private Long targetId;
    private String targetSnapshot;
    private String aiResponse;
    private Integer success;
    private String errorMsg;
    private Long executeTimeMs;
    private LocalDateTime createTime;
    private Integer deleted;
}
