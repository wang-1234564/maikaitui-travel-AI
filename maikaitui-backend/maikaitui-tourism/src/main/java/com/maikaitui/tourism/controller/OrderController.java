package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Order;
import com.maikaitui.tourism.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * @param payNow 是否立即支付，true=已支付，false=待支付
     */
    @PostMapping
    public Result createOrder(@Valid @RequestBody Order order,
                              @RequestHeader("X-User-Id") Long userId,
                              @RequestParam(defaultValue = "false") boolean payNow) {
        order.setUserId(userId);
        return orderService.createOrder(order, payNow);
    }

    /**
     * 分页查询订单列表（支持按状态筛选）
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestHeader("X-User-Id") Long userId,
                               @RequestParam(required = false) String status) {
        return orderService.getOrderList(page, size, userId, status);
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{id}")
    public Result getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    /**
     * 更新订单状态（管理端）
     */
    @PutMapping("/{id}/status")
    public Result updateOrderStatus(@PathVariable Long id, @RequestParam(required = false) String status,
                                     @RequestBody(required = false) java.util.Map<String, String> body) {
        String newStatus = status != null ? status : (body != null ? body.get("status") : null);
        return orderService.updateOrderStatus(id, newStatus);
    }

    /**
     * 管理端 — 查询全部订单（无需传 userId，支持多条件筛选）
     */
    @GetMapping("/admin/list")
    public Result getAllOrders(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String orderNo,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String orderStatus,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        return orderService.getAllOrders(page, size, orderNo, username, orderStatus, startDate, endDate);
    }

    /**
     * 取消订单（软删除，deleted=1）
     */
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    /**
     * 支付订单（pending → paid）
     */
    @PutMapping("/{id}/pay")
    public Result payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }
}
