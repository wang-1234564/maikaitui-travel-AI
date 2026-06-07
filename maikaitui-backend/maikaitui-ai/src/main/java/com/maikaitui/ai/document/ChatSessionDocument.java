package com.maikaitui.ai.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话 — MongoDB 文档
 * 使用嵌入式模型：消息内嵌在会话中，一次查询获取完整对话
 */
@Data
@Document(collection = "chat_sessions")
public class ChatSessionDocument {

    @Id
    private String id;

    private Long userId;

    private String title;

    private Boolean starred = false;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 对话摘要 — 最后一条助手回复的前80字符，列表展示用 */
    private String summary;

    /** 对话消息 — 内嵌文档 */
    private List<ChatMessage> messages = new ArrayList<>();

    @Data
    public static class ChatMessage {
        private String role;       // "user" 或 "assistant"
        private String content;
        private LocalDateTime createTime;
    }
}
