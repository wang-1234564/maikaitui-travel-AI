package com.maikaitui.tourism.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Order;
import com.maikaitui.tourism.mapper.OrderMapper;
import com.maikaitui.tourism.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(Order order, boolean payNow) {
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setOrderStatus(payNow ? "paid" : "pending");
        int rows = orderMapper.insert(order);
        if (rows > 0) {
            log.info("创建订单成功: {}, status={}", orderNo, order.getOrderStatus());
            return Result.success("创建订单成功", order);
        }
        return Result.error("创建订单失败");
    }

    /**
     * 生成订单编号：MKT + 时间戳 + 4位随机码
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
        return "MKT" + dateStr + randomStr;
    }

    @Override
    public Result getOrderList(int page, int size, Long userId, String status) {
        // 已取消订单：绕过 @TableLogic，手动分页
        if ("cancelled".equalsIgnoreCase(status)) {
            long offset = (long) (page - 1) * size;
            List<Order> records = orderMapper.selectCancelledOrders(userId, offset, size);
            long total = orderMapper.countCancelledOrders(userId);

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", records);
            pageData.put("total", total);
            pageData.put("current", page);
            pageData.put("size", size);
            pageData.put("pages", (total + size - 1) / size);
            return Result.success(pageData);
        }

        // 正常订单（pending/paid/completed）：@TableLogic 自动过滤 deleted=0
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            wrapper.eq(Order::getOrderStatus, status.toLowerCase());
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @Override
    public Result getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateOrderStatus(Long id, String status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setOrderStatus(status);
        orderMapper.updateById(order);
        log.info("更新订单状态成功: id={}, status={}", id, status);
        return Result.success("订单状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelOrder(Long id) {
        int rows = orderMapper.cancelOrder(id);
        if (rows > 0) {
            log.info("取消订单成功(软删除): id={}", id);
            return Result.success("订单取消成功");
        }
        return Result.error("订单不存在或状态不允许取消");
    }

    @Override
    public Result getAllOrders(int page, int size, String orderNo, String username,
                                String orderStatus, String startDate, String endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 订单编号模糊搜索
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        // 订单状态筛选
        if (orderStatus != null && !orderStatus.isEmpty()) {
            wrapper.eq(Order::getOrderStatus, orderStatus);
        }
        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        }
        // 用户名筛选（通过 userId 匹配，需要查 sys_user 表获取 userId）
        if (username != null && !username.isEmpty()) {
            // 使用子查询：查找 username 匹配的 userId
            wrapper.apply("user_id IN (SELECT id FROM sys_user WHERE username LIKE {0})",
                    "%" + username + "%");
        }

        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result payOrder(Long id) {
        int rows = orderMapper.payOrder(id);
        if (rows > 0) {
            log.info("支付订单成功: id={}", id);
            return Result.success("支付成功");
        }
        return Result.error("订单不存在或状态不允许支付");
    }
}
