package com.maikaitui.system.service;

import com.maikaitui.common.core.Result;

/**
 * 系统日志服务接口
 */
public interface SysLogService {

    /**
     * 分页查询日志列表
     */
    Result getLogList(int page, int size, String logType, String keyword);
}
