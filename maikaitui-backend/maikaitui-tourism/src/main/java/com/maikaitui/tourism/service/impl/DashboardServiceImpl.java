package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.mapper.SysUserMapper;
import com.maikaitui.tourism.dto.DashboardData;
import com.maikaitui.tourism.entity.Attraction;
import com.maikaitui.tourism.entity.Category;
import com.maikaitui.tourism.entity.Order;
import com.maikaitui.tourism.mapper.AttractionMapper;
import com.maikaitui.tourism.mapper.CategoryMapper;
import com.maikaitui.tourism.mapper.OrderMapper;
import com.maikaitui.tourism.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SysUserMapper sysUserMapper;
    private final AttractionMapper attractionMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Result getDashboardData() {
        // 总数统计
        Long totalUsers = sysUserMapper.selectCount(null);
        Long totalAttractions = attractionMapper.selectCount(
                new LambdaQueryWrapper<Attraction>().eq(Attraction::getStatus, 1));

        // 今日订单
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart);
        Long todayOrders = orderMapper.selectCount(todayWrapper);

        // 总营收
        LambdaQueryWrapper<Order> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.in(Order::getOrderStatus, "paid", "completed");
        List<Order> paidOrders = orderMapper.selectList(paidWrapper);
        BigDecimal totalRevenue = paidOrders.stream()
                .map(o -> o.getTotalPrice() != null ? o.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 近7天订单趋势
        List<Map<String, Object>> orderTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<Order> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.ge(Order::getCreateTime, day.atStartOfDay())
                     .lt(Order::getCreateTime, day.plusDays(1).atStartOfDay());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day.format(fmt));
            item.put("count", orderMapper.selectCount(dayWrapper));
            orderTrend.add(item);
        }

        // 景点分类分布
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        List<Map<String, Object>> categoryDistribution = new ArrayList<>();
        for (Category cat : categories) {
            LambdaQueryWrapper<Attraction> catWrapper = new LambdaQueryWrapper<>();
            catWrapper.eq(Attraction::getCategoryId, cat.getId())
                      .eq(Attraction::getStatus, 1);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", cat.getName());
            item.put("value", attractionMapper.selectCount(catWrapper));
            categoryDistribution.add(item);
        }

        // 最近10条订单
        LambdaQueryWrapper<Order> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.orderByDesc(Order::getCreateTime).last("LIMIT 10");
        List<Order> recentList = orderMapper.selectList(recentWrapper);
        List<Map<String, Object>> recentOrders = recentList.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("orderNo", o.getOrderNo());
            m.put("attractionName", o.getAttractionName() != null ? o.getAttractionName() : "--");
            m.put("contactName", o.getContactName() != null ? o.getContactName() : "--");
            m.put("totalPrice", o.getTotalPrice());
            m.put("orderStatus", o.getOrderStatus());
            m.put("createTime", o.getCreateTime());
            return m;
        }).toList();

        DashboardData data = DashboardData.builder()
                .totalUsers(totalUsers)
                .totalAttractions(totalAttractions)
                .todayOrders(todayOrders)
                .totalRevenue(totalRevenue)
                .orderTrend(orderTrend)
                .categoryDistribution(categoryDistribution)
                .recentOrders(recentOrders)
                .build();

        return Result.success(data);
    }
}
