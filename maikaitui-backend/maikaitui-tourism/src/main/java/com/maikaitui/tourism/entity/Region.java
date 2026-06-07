package com.maikaitui.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 地区实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tourism_region")
public class Region extends BaseEntity {

    /**
     * 地区名称
     */
    private String name;

    /**
     * 父级地区ID
     */
    private Long parentId;

    /**
     * 层级：1-国家，2-省份，3-城市，4-区/县
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 子地区列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Region> children;
}
