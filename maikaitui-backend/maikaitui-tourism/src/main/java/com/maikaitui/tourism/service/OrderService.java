package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     * @param payNow 是否立即支付
     */
    Result createOrder(Order order, boolean payNow);

    /**
     * 分页查询订单列表（支持按状态筛选）
     */
    Result getOrderList(int page, int size, Long userId, String status);

    /**
     * 根据ID查询订单
     */
    Result getOrderById(Long id);

    /**
     * 更新订单状态
     */
    Result updateOrderStatus(Long id, String status);

    /**
     * 取消订单（软删除）
     */
    Result cancelOrder(Long id);

    /**
     * 支付订单
     */
    Result payOrder(Long id);

    /**
     * 管理端 — 查询全部订单（支持多条件筛选）
     */
    Result getAllOrders(int page, int size, String orderNo, String username,
                        String orderStatus, String startDate, String endDate);
}
