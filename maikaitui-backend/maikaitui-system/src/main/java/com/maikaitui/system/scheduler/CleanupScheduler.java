package com.maikaitui.system.scheduler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 软删除数据定时清理任务（XXL-JOB Handler）
 *
 * 物理删除所有表中 deleted = 1 的记录。
 * - 通过 information_schema 动态发现所有包含 deleted 列的表
 * - 使用原生 SQL 物理删除（绕过 MyBatis Plus @TableLogic 的查询过滤）
 *
 * JobHandler 名称: cleanupSoftDeletedHandler
 * 建议 Cron: 0 0 3 * * ?（每天凌晨 3:00）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_TABLES_SQL =
            "SELECT TABLE_NAME FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_NAME = 'deleted'";

    private static final String DELETE_TEMPLATE = "DELETE FROM `%s` WHERE deleted = 1";

    /**
     * 清理所有表中 deleted = 1 的软删除记录
     *
     * 在 XXL-JOB Admin 中配置 JobHandler = cleanupSoftDeletedHandler 即可调度。
     */
    @XxlJob("cleanupSoftDeletedHandler")
    public void cleanupSoftDeletedRecords() {
        XxlJobHelper.log("========== 软删除数据清理任务开始 ==========");

        try {
            // 1. 动态查询当前数据库中所有包含 deleted 列的表
            List<String> tables = jdbcTemplate.queryForList(FIND_TABLES_SQL, String.class);

            if (tables.isEmpty()) {
                XxlJobHelper.log("未发现包含 deleted 列的表，跳过清理");
                return;
            }

            XxlJobHelper.log("发现 " + tables.size() + " 张包含 deleted 列的表: " + tables);

            // 2. 逐表执行物理删除
            int grandTotal = 0;
            for (String table : tables) {
                try {
                    String deleteSql = String.format(DELETE_TEMPLATE, table);
                    int rows = jdbcTemplate.update(deleteSql);
                    if (rows > 0) {
                        XxlJobHelper.log("✓ 表 `" + table + "`: 删除 " + rows + " 条记录");
                        grandTotal += rows;
                    }
                } catch (Exception e) {
                    XxlJobHelper.log("✗ 表 `" + table + "` 清理失败: " + e.getMessage());
                    log.error("清理表 {} 失败", table, e);
                }
            }

            XxlJobHelper.log("========== 清理完成，共删除 " + grandTotal + " 条软删除记录 ==========");
            XxlJobHelper.handleSuccess("共删除 " + grandTotal + " 条记录");
        } catch (Exception e) {
            log.error("定时清理任务执行失败", e);
            XxlJobHelper.log("任务执行异常: " + e.getMessage());
            XxlJobHelper.handleFail("任务执行异常: " + e.getMessage());
        }
    }
}
