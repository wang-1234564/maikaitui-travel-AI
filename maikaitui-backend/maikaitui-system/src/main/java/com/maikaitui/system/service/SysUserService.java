package com.maikaitui.system.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysUser;

import java.util.List;

/**
 * 系统用户服务接口
 */
public interface SysUserService {

    /**
     * 分页查询用户列表
     */
    Result getUserList(int page, int size, String keyword);

    /**
     * 根据ID查询用户
     */
    Result getUserById(Long id);

    /**
     * 新增用户
     */
    Result addUser(SysUser user);

    /**
     * 修改用户
     */
    Result updateUser(SysUser user);

    /**
     * 删除用户
     */
    Result deleteUser(Long id);

    /**
     * 分配角色
     */
    Result assignRoles(Long userId, List<Long> roleIds);
}
