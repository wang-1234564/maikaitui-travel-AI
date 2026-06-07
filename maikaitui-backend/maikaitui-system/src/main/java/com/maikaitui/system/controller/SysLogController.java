package com.maikaitui.system.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.service.SysLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统日志控制器（只读）
 */
@Slf4j
@RestController
@RequestMapping("/api/system/log")
@AllArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    /**
     * 分页查询日志列表
     */
    @GetMapping("/list")
    public Result getLogList(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String logType,
                             @RequestParam(required = false) String keyword) {
        return sysLogService.getLogList(page, size, logType, keyword);
    }
}
