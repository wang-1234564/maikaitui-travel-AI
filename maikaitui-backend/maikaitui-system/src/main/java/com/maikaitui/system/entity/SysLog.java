package com.maikaitui.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maikaitui.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
public class SysLog extends BaseEntity {

    /**
     * 日志类型：operation-操作日志，login-登录日志
     */
    private String logType;

    /**
     * 日志标题
     */
    private String title;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 远程IP
     */
    private String remoteAddr;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 执行时间（毫秒）
     */
    private Long executeTime;

    /**
     * 操作用户名
     */
    private String username;
}
