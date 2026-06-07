package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_order")
public class Order extends BaseEntity {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 景点名称
     */
    private String attractionName;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 订单状态：pending-待支付，paid-已支付，cancelled-已取消，completed-已完成
     */
    private String orderStatus;

    /**
     * 游览日期
     */
    private LocalDate visitDate;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 备注
     */
    private String remark;
}
