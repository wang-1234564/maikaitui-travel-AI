package com.maikaitui.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysRole;
import com.maikaitui.system.entity.SysRoleMenu;
import com.maikaitui.system.mapper.SysRoleMapper;
import com.maikaitui.system.mapper.SysRoleMenuMapper;
import com.maikaitui.system.service.SysRoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public Result getRoleList(int page, int size) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRole::getCreateTime);
        IPage<SysRole> result = sysRoleMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @Override
    public Result getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1)
                .orderByDesc(SysRole::getCreateTime);
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return Result.success(roles);
    }

    @Override
    public Result getRoleById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addRole(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, role.getRoleCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            return Result.error("角色编码已存在");
        }
        sysRoleMapper.insert(role);
        return Result.success("新增角色成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateRole(SysRole role) {
        if (sysRoleMapper.selectById(role.getId()) == null) {
            return Result.error("角色不存在");
        }
        sysRoleMapper.updateById(role);
        return Result.success("修改角色成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteRole(Long id) {
        if (sysRoleMapper.selectById(id) == null) {
            return Result.error("角色不存在");
        }
        sysRoleMapper.deleteById(id);
        // 删除角色菜单关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, id);
        sysRoleMenuMapper.delete(wrapper);
        return Result.success("删除角色成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有菜单关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(wrapper);
        // 插入新的菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenus = menuIds.stream().map(menuId -> {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            }).collect(Collectors.toList());
            for (SysRoleMenu roleMenu : roleMenus) {
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return Result.success("分配菜单成功");
    }
}
