package com.maikaitui.system.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysMenu;
import com.maikaitui.system.service.SysMenuService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统菜单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/menu")
@AllArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result getMenuTree() {
        return sysMenuService.getMenuTree();
    }

    /**
     * 根据ID查询菜单
     */
    @GetMapping("/{id}")
    public Result getMenuById(@PathVariable Long id) {
        return sysMenuService.getMenuById(id);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    public Result addMenu(@Valid @RequestBody SysMenu menu) {
        return sysMenuService.addMenu(menu);
    }

    /**
     * 修改菜单
     */
    @PutMapping
    public Result updateMenu(@Valid @RequestBody SysMenu menu) {
        return sysMenuService.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result deleteMenu(@PathVariable Long id) {
        return sysMenuService.deleteMenu(id);
    }

    /**
     * 根据角色ID查询菜单列表
     */
    @GetMapping("/role/{roleId}")
    public Result getMenusByRoleId(@PathVariable Long roleId) {
        return sysMenuService.getMenusByRoleId(roleId);
    }
}
