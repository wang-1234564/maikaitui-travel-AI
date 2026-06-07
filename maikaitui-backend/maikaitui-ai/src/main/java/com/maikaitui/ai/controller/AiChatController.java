package com.maikaitui.ai.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.maikaitui.ai.document.ChatSessionDocument;
import com.maikaitui.ai.document.ChatSessionDocument.ChatMessage;
import com.maikaitui.ai.entity.dto.AiChatRequest;
import com.maikaitui.ai.repository.ChatSessionRepository;
import com.maikaitui.ai.service.AiCacheService;
import com.maikaitui.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatClient chatClient;
    private final ChatSessionRepository sessionRepository;
    private final AiCacheService aiCacheService;

    /** 历史消息保留上限：最近 10 轮对话（20 条） */
    private static final int MAX_HISTORY_MESSAGES = 20;

    @PostMapping("/chat")
    @SentinelResource(value = "ai-chat", blockHandler = "chatBlockHandler")
    public Result chat(@RequestBody AiChatRequest request,
                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("AI chat request: {}", request.getMessage());

        // ===== 1. 加载或创建会话 =====
        ChatSessionDocument session = null;

        if (userId != null) {
            if (request.getSessionId() != null) {
                Optional<ChatSessionDocument> opt = sessionRepository.findById(request.getSessionId().toString());
                if (opt.isEmpty()) {
                    return Result.error("会话不存在");
                }
                session = opt.get();
                if (!session.getUserId().equals(userId)) {
                    return Result.error("无权访问该会话");
                }
            } else {
                session = new ChatSessionDocument();
                session.setUserId(userId);
                String title = request.getMessage().length() > 30
                        ? request.getMessage().substring(0, 30) + "..."
                        : request.getMessage();
                session.setTitle(title);
                session.setCreateTime(LocalDateTime.now());
            }
        }

        // ===== 2. 构建消息列表（历史 + 当前） =====
        List<Message> aiMessages = new ArrayList<>();

        if (session != null && !session.getMessages().isEmpty()) {
            List<ChatMessage> history = session.getMessages();
            int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
            for (int i = start; i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                if ("user".equals(msg.getRole())) {
                    aiMessages.add(new UserMessage(msg.getContent()));
                } else {
                    aiMessages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }
        // 注入景区上下文（前端从详情页传入）
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            aiMessages.add(0, new SystemMessage(
                "【用户当前浏览景区】" + request.getContext() +
                "。请结合该景区的信息回答用户问题，给出具体、个性化的建议。"));
        }
        aiMessages.add(new UserMessage(request.getMessage()));

        // ===== 3. 调用 AI（优先走缓存） =====
        String reply;
        // 仅无历史上下文时使用缓存（单独提问，非多轮对话）
        boolean hasContext = request.getContext() != null && !request.getContext().isEmpty();
        boolean noHistory = session == null || session.getMessages().isEmpty();
        if (noHistory && !hasContext) {
            String cached = aiCacheService.get(request.getMessage());
            if (cached != null) {
                reply = cached;
                log.info("AI 缓存命中，跳过 API 调用");
            } else {
                reply = chatClient.prompt().messages(aiMessages).call().content();
                aiCacheService.put(request.getMessage(), reply);
            }
        } else {
            reply = chatClient.prompt().messages(aiMessages).call().content();
        }
        String sessionId = null;

        if (session != null) {
            LocalDateTime now = LocalDateTime.now();

            ChatMessage userMsg = new ChatMessage();
            userMsg.setRole("user");
            userMsg.setContent(request.getMessage());
            userMsg.setCreateTime(now);
            session.getMessages().add(userMsg);

            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(reply);
            assistantMsg.setCreateTime(now);
            session.getMessages().add(assistantMsg);

            String summary = reply.length() > 80 ? reply.substring(0, 80) + "..." : reply;
            session.setSummary(summary);
            session.setUpdateTime(now);

            session = sessionRepository.save(session);
            sessionId = session.getId();
        }

        Map<String, Object> respData = new HashMap<>();
        respData.put("reply", reply);
        respData.put("timestamp", LocalDateTime.now().toString());
        if (sessionId != null) {
            respData.put("sessionId", sessionId);
        }
        return Result.success(respData);
    }

    /** Sentinel 兜底处理 */
    public static Result chatBlockHandler(AiChatRequest request, Long userId, BlockException ex) {
        return Result.error("网络繁忙，请稍后再试");
    }

    @GetMapping("/sessions")
    public Result sessions(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.success(List.of());
        }
        List<ChatSessionDocument> sessions = sessionRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        List<Map<String, Object>> result = sessions.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("userId", s.getUserId());
            m.put("starred", s.getStarred());
            m.put("summary", s.getSummary());
            m.put("createTime", s.getCreateTime());
            m.put("updateTime", s.getUpdateTime());
            return m;
        }).toList();
        return Result.success(result);
    }

    @GetMapping("/history")
    public Result history(@RequestParam String sessionId,
                          @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.success(List.of());
        }
        Optional<ChatSessionDocument> opt = sessionRepository.findById(sessionId);
        if (opt.isEmpty()) {
            return Result.success(List.of());
        }
        ChatSessionDocument session = opt.get();
        if (!session.getUserId().equals(userId)) {
            return Result.error("无权访问该会话");
        }
        List<Map<String, Object>> result = session.getMessages().stream().map(msg -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", "assistant".equals(msg.getRole()) ? "assistant" : "user");
            m.put("content", msg.getContent());
            m.put("createTime", msg.getCreateTime());
            return m;
        }).toList();
        return Result.success(result);
    }

    @DeleteMapping("/session/{id}")
    public Result deleteSession(@PathVariable String id,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.success();
        }
        Optional<ChatSessionDocument> opt = sessionRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.success();
        }
        if (!opt.get().getUserId().equals(userId)) {
            return Result.error("无权删除该会话");
        }
        sessionRepository.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/history")
    public Result clearAll(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.success();
        }
        sessionRepository.deleteByUserId(userId);
        return Result.success();
    }

    @GetMapping("/recommend")
    public Result recommend(@RequestParam(required = false) String preference) {
        log.info("AI recommend request, preference: {}", preference);
        return Result.success(Map.of("recommendations", List.of(), "message", "AI推荐功能即将上线，敬请期待！"));
    }

    @GetMapping("/health")
    public Result health() {
        return Result.success(Map.of("status", "ok", "service", "maikaitui-ai", "timestamp", LocalDateTime.now().toString()));
    }
}
