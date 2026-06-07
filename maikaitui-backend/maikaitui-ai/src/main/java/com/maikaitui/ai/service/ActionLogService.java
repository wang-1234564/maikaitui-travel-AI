package com.maikaitui.ai.service;

import com.maikaitui.ai.entity.AdminAiActionLog;
import com.maikaitui.ai.mapper.AdminAiActionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI 操作日志服务 — 记录每次 AI Tool 调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionLogService {

    private final AdminAiActionLogMapper actionLogMapper;

    public void log(String actionType, String actionDesc, String targetType,
                    Long targetId, String aiResponse, boolean success, long executeTimeMs) {
        AdminAiActionLog logEntry = new AdminAiActionLog();
        logEntry.setActionType(actionType);
        logEntry.setActionDesc(actionDesc);
        logEntry.setTargetType(targetType);
        logEntry.setTargetId(targetId);
        logEntry.setAiResponse(aiResponse);
        logEntry.setSuccess(success ? 1 : 0);
        logEntry.setExecuteTimeMs(executeTimeMs);
        logEntry.setCreateTime(LocalDateTime.now());
        actionLogMapper.insert(logEntry);
    }
}
