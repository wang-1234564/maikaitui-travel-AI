package com.maikaitui.tourism.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {

    /** 总用户数 */
    private Long totalUsers;
    /** 总景点数 */
    private Long totalAttractions;
    /** 今日订单数 */
    private Long todayOrders;
    /** 总营收 */
    private BigDecimal totalRevenue;

    /** 订单趋势（近7天）: [{date, count}] */
    private List<Map<String, Object>> orderTrend;

    /** 分类分布: [{name, value}] */
    private List<Map<String, Object>> categoryDistribution;

    /** 最近订单列表 */
    private List<Map<String, Object>> recentOrders;
}
