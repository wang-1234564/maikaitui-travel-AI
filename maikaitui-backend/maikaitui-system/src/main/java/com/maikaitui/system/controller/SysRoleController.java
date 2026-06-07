package com.maikaitui.system.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysRole;
import com.maikaitui.system.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/role")
@AllArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    public Result getRoleList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return sysRoleService.getRoleList(page, size);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/all")
    public Result getAllRoles() {
        return sysRoleService.getAllRoles();
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    public Result getRoleById(@PathVariable Long id) {
        return sysRoleService.getRoleById(id);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result addRole(@Valid @RequestBody SysRole role) {
        return sysRoleService.addRole(role);
    }

    /**
     * 修改角色
     */
    @PutMapping
    public Result updateRole(@Valid @RequestBody SysRole role) {
        return sysRoleService.updateRole(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result deleteRole(@PathVariable Long id) {
        return sysRoleService.deleteRole(id);
    }

    /**
     * 分配菜单权限
     */
    @PostMapping("/{id}/menus")
    public Result assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        return sysRoleService.assignMenus(id, menuIds);
    }
}
