package com.maikaitui.system.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysUser;
import com.maikaitui.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@AllArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result getUserList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {
        return sysUserService.getUserList(page, size, keyword);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result addUser(@Valid @RequestBody SysUser user) {
        return sysUserService.addUser(user);
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result updateUser(@Valid @RequestBody SysUser user) {
        return sysUserService.updateUser(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        return sysUserService.deleteUser(id);
    }

    /**
     * 分配角色
     */
    @PostMapping("/{id}/roles")
    public Result assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        return sysUserService.assignRoles(id, roleIds);
    }
}
