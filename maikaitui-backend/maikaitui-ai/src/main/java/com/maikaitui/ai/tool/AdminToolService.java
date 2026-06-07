package com.maikaitui.ai.tool;

import com.maikaitui.ai.entity.AttractionEntity;
import com.maikaitui.ai.entity.CommentEntity;
import com.maikaitui.ai.mapper.AttractionMapper;
import com.maikaitui.ai.mapper.CategoryMapper;
import com.maikaitui.ai.mapper.CommentMapper;
import com.maikaitui.ai.mapper.RegionMapper;
import com.maikaitui.ai.service.ActionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 AI 工具服务 — 所有 AI 可调用的管理功能
 *
 * <p>每个 {@link Tool @Tool} 方法会被 Spring AI 自动扫描并注册为 LLM 可调用工具。
 * 方法的 {@code description} 和参数的 {@link ToolParam @ToolParam} 描述会被 LLM 读取，
 * 用于判断何时调用哪个工具。
 *
 * <p><b>扩展新功能只需一步：添加一个 @Tool 方法，重启即生效。</b>
 * 无需修改配置类、控制器或前端代码。
 *
 * <p>示例 — 添加"生成运营报告"：
 * <pre>{@code
 * @Tool(description = "生成指定日期范围的运营报告")
 * public String generateReport(
 *         @ToolParam(description = "开始日期 yyyy-MM-dd") String startDate,
 *         @ToolParam(description = "结束日期 yyyy-MM-dd") String endDate) {
 *     return "报告...";
 * }
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminToolService {

    private final CommentMapper commentMapper;
    private final AttractionMapper attractionMapper;
    private final RegionMapper regionMapper;
    private final CategoryMapper categoryMapper;
    private final ActionLogService actionLogService;

    // ==================== 评论审核工具 ====================

    @Tool(description = "获取最近待审核的评论列表。返回每条评论的id、用户名、内容、评分、创建时间。调用后应对每条评论分析其是否存在违规内容")
    public List<Map<String, Object>> getPendingComments(
            @ToolParam(description = "返回数量，默认10，最大50") int limit) {
        if (limit <= 0) limit = 10;
        if (limit > 50) limit = 50;
        List<Map<String, Object>> comments = commentMapper.selectPendingComments(limit);
        log.info("Tool[getPendingComments] returned {} comments", comments.size());
        actionLogService.log("comment_query", "查询待审核评论", "comment", null,
                "返回" + comments.size() + "条待审核评论", true, 0);
        return comments;
    }

    @Tool(description = "按关键字搜索评论内容，用于查找包含特定敏感词的评论。返回匹配的评论列表")
    public List<Map<String, Object>> searchComments(
            @ToolParam(description = "搜索关键字，如'广告'、'加微信'、'最便宜'") String keyword,
            @ToolParam(description = "返回数量，默认20，最大50") int limit) {
        if (limit <= 0) limit = 20;
        if (limit > 50) limit = 50;
        List<Map<String, Object>> comments = commentMapper.searchByKeyword(keyword, limit);
        log.info("Tool[searchComments] keyword={}, returned {} comments", keyword, comments.size());
        actionLogService.log("comment_query", "搜索评论: " + keyword, "comment", null,
                "返回" + comments.size() + "条", true, 0);
        return comments;
    }

    @Tool(description = "通过评论审核，将评论标记为已通过。用于无违规内容的正常评论")
    public String approveComment(
            @ToolParam(description = "评论ID") Long commentId,
            @ToolParam(description = "审核备注（可选）") String note) {
        int rows = commentMapper.updateStatus(commentId, "approved", note, 10);
        log.info("Tool[approveComment] id={}, note={}", commentId, note);
        actionLogService.log("comment_approve", "通过审核: " + (note != null ? note : ""),
                "comment", commentId, "审核通过", rows > 0, 0);
        return rows > 0
                ? "评论 #" + commentId + " 已审核通过" + (note != null ? "，备注：" + note : "")
                : "评论 #" + commentId + " 不存在或已处理";
    }

    @Tool(description = "拒绝评论并标记为违规。适用于包含敏感词、广告推销、人身攻击、不当言论的评论")
    public String rejectComment(
            @ToolParam(description = "评论ID") Long commentId,
            @ToolParam(description = "违规原因，如'含广告内容'、'使用侮辱性言论'") String reason) {
        int rows = commentMapper.updateStatus(commentId, "rejected", reason, 90);
        log.info("Tool[rejectComment] id={}, reason={}", commentId, reason);
        actionLogService.log("comment_reject", "拒绝评论: " + (reason != null ? reason : ""),
                "comment", commentId, "标记违规", rows > 0, 0);
        return rows > 0
                ? "评论 #" + commentId + " 已标记为违规，原因：" + reason
                : "评论 #" + commentId + " 不存在或已处理";
    }

    @Tool(description = "永久删除严重违规评论。不可恢复！仅用于政治敏感、色情低俗等极端违规内容。删除前会自动保存操作日志")
    public String deleteComment(
            @ToolParam(description = "评论ID") Long commentId,
            @ToolParam(description = "删除原因，如'含色情低俗内容'、'政治敏感言论'") String reason) {
        CommentEntity comment = commentMapper.selectById(commentId);
        String snapshot = comment != null ? comment.getContent() : "N/A";
        if (snapshot.length() > 200) {
            snapshot = snapshot.substring(0, 200) + "...";
        }
        actionLogService.log("comment_delete", "删除评论: " + (reason != null ? reason : ""),
                "comment", commentId, "内容快照: " + snapshot, true, 0);

        int rows = commentMapper.deleteById(commentId);
        log.warn("Tool[deleteComment] DELETED id={}, reason={}", commentId, reason);
        return rows > 0
                ? "评论 #" + commentId + " 已永久删除（原因：" + reason + "）"
                : "评论 #" + commentId + " 不存在";
    }

    @Tool(description = "获取评论统计数据，返回总数、待审核数、已通过数、已拒绝数。用于了解当前审核进度")
    public Map<String, Object> getCommentStats() {
        Map<String, Object> stats = commentMapper.getCommentStats();
        log.info("Tool[getCommentStats] returned stats: {}", stats);
        actionLogService.log("comment_stats", "查询评论统计", "comment", null,
                stats.toString(), true, 0);
        return stats;
    }

    // ==================== 景区数据工具 ====================

    @Tool(description = "查询系统中已有的地区（城市）和分类列表，包含ID和名称。在添加景区前应先调用此工具，获取可用的 regionId 和 categoryId")
    public Map<String, Object> getRegionsAndCategories() {
        List<Map<String, Object>> cities = regionMapper.selectCities();
        List<Map<String, Object>> categories = categoryMapper.selectTopCategories();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cities", cities);
        result.put("categories", categories);
        log.info("Tool[getRegionsAndCategories] cities={}, categories={}", cities.size(), categories.size());
        return result;
    }

    @Tool(description = "添加一个新景区到数据库。你应根据自己的知识填充景区的真实信息（名称、描述、地址、门票价格、坐标、开放时间等）。regionId 和 categoryId 需先通过 getRegionsAndCategories 获取")
    public String addAttraction(
            @ToolParam(description = "景区名称") String name,
            @ToolParam(description = "景区详细描述，200-500字，介绍特色、历史、看点") String description,
            @ToolParam(description = "详细地址") String address,
            @ToolParam(description = "门票价格（元）") BigDecimal price,
            @ToolParam(description = "所属地区ID（城市），从 getRegionsAndCategories 返回的 cities 中选取") Long regionId,
            @ToolParam(description = "所属分类ID，从 getRegionsAndCategories 返回的 categories 中选取。1=自然风光 2=历史文化 3=主题乐园 4=博物馆 5=宗教场所 6=古镇村落 7=城市地标") Long categoryId,
            @ToolParam(description = "纬度，如 29.3288") Double latitude,
            @ToolParam(description = "经度，如 110.4218") Double longitude,
            @ToolParam(description = "开放时间，如 '08:00-18:00'") String openTime,
            @ToolParam(description = "封面图片URL（可选）") String coverImage) {

        AttractionEntity attraction = new AttractionEntity();
        attraction.setName(name);
        attraction.setDescription(description);
        attraction.setAddress(address);
        attraction.setPrice(price);
        attraction.setRegionId(regionId);
        attraction.setCategoryId(categoryId);
        attraction.setLatitude(latitude);
        attraction.setLongitude(longitude);
        attraction.setOpenTime(openTime);
        attraction.setCoverImage(coverImage);
        attraction.setRating(5.0);
        attraction.setViewCount(0L);
        attraction.setLikeCount(0L);
        attraction.setIsHot(0);
        attraction.setStatus(1);  // 已发布
        attraction.setDeleted(0);
        attraction.setCreateTime(LocalDateTime.now());
        attraction.setUpdateTime(LocalDateTime.now());

        int rows = attractionMapper.insert(attraction);
        log.info("Tool[addAttraction] name={}, id={}, rows={}", name, attraction.getId(), rows);
        actionLogService.log("attraction_add", "添加景区: " + name,
                "attraction", attraction.getId(), "价格: ¥" + price, rows > 0, 0);
        return rows > 0
                ? "景区「" + name + "」添加成功！ID: " + attraction.getId() + "，价格: ¥" + price
                : "景区「" + name + "」添加失败";
    }

    @Tool(description = "查询最近添加的景区列表，用于确认已添加的数据")
    public List<Map<String, Object>> getRecentAttractions(
            @ToolParam(description = "返回数量，默认10") int limit) {
        if (limit <= 0) limit = 10;
        return attractionMapper.selectRecentAttractions(limit);
    }
}
