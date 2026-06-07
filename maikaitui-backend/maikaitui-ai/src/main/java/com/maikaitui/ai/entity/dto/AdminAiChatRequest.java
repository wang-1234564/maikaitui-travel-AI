package com.maikaitui.ai.entity.dto;

import lombok.Data;

/**
 * 管理员 AI 对话请求 DTO
 */
@Data
public class AdminAiChatRequest {
    private String message;
    private String conversationId;
}
