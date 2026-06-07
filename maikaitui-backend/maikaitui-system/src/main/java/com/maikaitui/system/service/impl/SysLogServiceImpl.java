package com.maikaitui.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysLog;
import com.maikaitui.system.mapper.SysLogMapper;
import com.maikaitui.system.service.SysLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统日志服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysLogServiceImpl implements SysLogService {

    private final SysLogMapper sysLogMapper;

    @Override
    public Result getLogList(int page, int size, String logType, String keyword) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(logType)) {
            wrapper.eq(SysLog::getLogType, logType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(SysLog::getTitle, keyword)
                    .or()
                    .like(SysLog::getUsername, keyword)
                    .or()
                    .like(SysLog::getRequestUri, keyword));
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        IPage<SysLog> result = sysLogMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }
}
