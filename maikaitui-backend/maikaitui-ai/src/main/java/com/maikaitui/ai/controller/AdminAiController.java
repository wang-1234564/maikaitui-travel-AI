package com.maikaitui.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.ai.entity.AdminAiActionLog;
import com.maikaitui.ai.entity.dto.AdminAiChatRequest;
import com.maikaitui.ai.mapper.AdminAiActionLogMapper;
import com.maikaitui.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员 AI 控制台控制器
 *
 * <p>路径 /api/admin/ai/* — 需要 JWT 认证（不在 Gateway 白名单中）
 * <p>使用独立的 adminChatClient（注册了管理工具的 ChatClient）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    private final ChatClient adminChatClient;
    private final AdminAiActionLogMapper actionLogMapper;

    public AdminAiController(
            @Qualifier("adminChatClient") ChatClient adminChatClient,
            AdminAiActionLogMapper actionLogMapper) {
        this.adminChatClient = adminChatClient;
        this.actionLogMapper = actionLogMapper;
    }

    /** 最大保留历史消息数 */
    private static final int MAX_HISTORY = 20;

    /** 简单的内存对话存储（会话ID → 消息历史） */
    private final Map<String, List<Message>> conversationStore = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Message>> eldest) {
            return size() > 100;
        }
    };

    /**
     * 管理员 AI 对话
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody AdminAiChatRequest request,
                       @RequestHeader("X-User-Id") Long userId,
                       @RequestHeader(value = "X-Username", required = false) String username) {
        log.info("Admin AI chat: user={}, message={}", username, request.getMessage());

        // 1. 加载或创建对话
        String conversationId = request.getConversationId() != null
                ? request.getConversationId() : UUID.randomUUID().toString();
        List<Message> history = conversationStore.computeIfAbsent(conversationId,
                k -> new ArrayList<>());

        // 2. 构建消息列表（历史 + 当前）
        List<Message> promptMessages = new ArrayList<>();
        int start = Math.max(0, history.size() - MAX_HISTORY);
        promptMessages.addAll(history.subList(start, history.size()));
        promptMessages.add(new UserMessage(request.getMessage()));

        // 3. 调用 AI（Spring AI 自动处理 Tool Calling 循环）
        long startTime = System.currentTimeMillis();
        String reply;
        try {
            reply = adminChatClient.prompt()
                    .messages(promptMessages)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Admin AI chat error", e);
            return Result.error("AI 服务异常: " + e.getMessage());
        }
        long elapsed = System.currentTimeMillis() - startTime;

        // 4. 保存对话历史
        history.add(new UserMessage(request.getMessage()));
        history.add(new AssistantMessage(reply));
        conversationStore.put(conversationId, history);

        // 5. 返回响应
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reply", reply);
        data.put("conversationId", conversationId);
        data.put("timestamp", LocalDateTime.now().toString());
        data.put("elapsedMs", elapsed);

        return Result.success(data);
    }

    /**
     * 获取 AI 操作日志（分页）
     */
    @GetMapping("/actions")
    public Result getActionLogs(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        Page<AdminAiActionLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AdminAiActionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AdminAiActionLog::getCreateTime);
        Page<AdminAiActionLog> result = actionLogMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result health() {
        return Result.success(Map.of(
                "status", "ok",
                "service", "maikaitui-ai-admin",
                "tools", List.of(
                        "getPendingComments", "searchComments",
                        "approveComment", "rejectComment", "deleteComment",
                        "getCommentStats", "getRegionsAndCategories",
                        "addAttraction", "getRecentAttractions"),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
