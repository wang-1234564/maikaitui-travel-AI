package com.maikaitui.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.tourism.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 支付订单：pending → paid（绕过 @TableLogic，直接用原生 SQL）
     */
    @Update("UPDATE tourism_order SET order_status = 'paid' WHERE id = #{id} AND deleted = 0 AND order_status = 'pending'")
    int payOrder(@Param("id") Long id);

    /**
     * 取消订单：设置 cancelled + 软删除（绕过 @TableLogic，直接用原生 SQL）
     */
    @Update("UPDATE tourism_order SET order_status = 'cancelled', deleted = 1 WHERE id = #{id} AND deleted = 0 AND order_status = 'pending'")
    int cancelOrder(@Param("id") Long id);

    /**
     * 查询已取消订单列表（绕过 @TableLogic 的 deleted=0 过滤）
     */
    @Select("SELECT * FROM tourism_order WHERE user_id = #{userId} AND deleted = 1 ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Order> selectCancelledOrders(@Param("userId") Long userId,
                                      @Param("offset") long offset,
                                      @Param("limit") long limit);

    /**
     * 统计已取消订单数量
     */
    @Select("SELECT COUNT(*) FROM tourism_order WHERE user_id = #{userId} AND deleted = 1")
    long countCancelledOrders(@Param("userId") Long userId);
}
