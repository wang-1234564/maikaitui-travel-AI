package com.maikaitui.system.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysMenu;

/**
 * 系统菜单服务接口
 */
public interface SysMenuService {

    /**
     * 获取菜单树
     */
    Result getMenuTree();

    /**
     * 根据ID查询菜单
     */
    Result getMenuById(Long id);

    /**
     * 新增菜单
     */
    Result addMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    Result updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    Result deleteMenu(Long id);

    /**
     * 根据角色ID查询菜单列表
     */
    Result getMenusByRoleId(Long roleId);
}
