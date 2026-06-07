package com.maikaitui.ai.jobhandler;

import com.maikaitui.ai.document.ChatSessionDocument;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * AI 对话记录定时清理任务（XXL-JOB Handler）
 *
 * 每月清理一次 MongoDB chat_sessions 集合中的所有对话记录。
 *
 * JobHandler 名称: cleanupChatHistoryHandler
 * 建议 Cron: 0 0 2 1 * ?（每月 1 号凌晨 2:00）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCleanupJob {

    private final MongoTemplate mongoTemplate;

    /**
     * 清理所有对话会话记录
     *
     * 使用 MongoTemplate.remove() 高效删除整个集合的数据，
     * 比 MongoRepository.deleteAll() 逐条删除性能更好。
     */
    @XxlJob("cleanupChatHistoryHandler")
    public void cleanupChatHistory() {
        XxlJobHelper.log("========== AI 对话记录清理任务开始 ==========");

        try {
            // 删除前统计数量
            long count = mongoTemplate.count(new Query(), ChatSessionDocument.class);
            XxlJobHelper.log("当前 chat_sessions 集合中共有 " + count + " 条对话记录");

            if (count == 0) {
                XxlJobHelper.log("无数据需要清理，任务结束");
                XxlJobHelper.handleSuccess("无数据，跳过清理");
                return;
            }

            // 物理删除所有记录
            long deleted = mongoTemplate.remove(new Query(), ChatSessionDocument.class).getDeletedCount();

            XxlJobHelper.log("========== 清理完成，共删除 " + deleted + " 条对话记录 ==========");
            XxlJobHelper.handleSuccess("共删除 " + deleted + " 条对话记录");
        } catch (Exception e) {
            log.error("AI 对话清理任务执行失败", e);
            XxlJobHelper.log("任务执行异常: " + e.getMessage());
            XxlJobHelper.handleFail("任务执行异常: " + e.getMessage());
        }
    }
}
