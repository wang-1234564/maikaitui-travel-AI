package com.maikaitui.system.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysRole;

import java.util.List;

/**
 * 系统角色服务接口
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     */
    Result getRoleList(int page, int size);

    /**
     * 查询所有角色
     */
    Result getAllRoles();

    /**
     * 根据ID查询角色
     */
    Result getRoleById(Long id);

    /**
     * 新增角色
     */
    Result addRole(SysRole role);

    /**
     * 修改角色
     */
    Result updateRole(SysRole role);

    /**
     * 删除角色
     */
    Result deleteRole(Long id);

    /**
     * 分配菜单权限
     */
    Result assignMenus(Long roleId, List<Long> menuIds);
}
