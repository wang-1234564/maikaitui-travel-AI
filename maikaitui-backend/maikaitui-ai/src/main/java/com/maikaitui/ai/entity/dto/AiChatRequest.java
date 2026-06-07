package com.maikaitui.ai.entity.dto;

import lombok.Data;

@Data
public class AiChatRequest {

    private String message;
    private String sessionId;
    private String context; // 可选：景区上下文信息（前端传）
}
