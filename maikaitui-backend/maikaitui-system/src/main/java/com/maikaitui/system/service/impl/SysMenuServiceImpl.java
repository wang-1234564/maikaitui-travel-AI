package com.maikaitui.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysMenu;
import com.maikaitui.system.entity.SysRoleMenu;
import com.maikaitui.system.mapper.SysMenuMapper;
import com.maikaitui.system.mapper.SysRoleMenuMapper;
import com.maikaitui.system.service.SysMenuService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统菜单服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public Result getMenuTree() {
        // 查询所有菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSortOrder);
        List<SysMenu> allMenus = sysMenuMapper.selectList(wrapper);

        // 构建树形结构
        List<SysMenu> menuTree = buildMenuTree(allMenus, 0L);
        return Result.success(menuTree);
    }

    /**
     * 递归构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> children = menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(parentId))
                .collect(Collectors.toList());

        for (SysMenu menu : children) {
            List<SysMenu> subChildren = buildMenuTree(menus, menu.getId());
            menu.setChildren(subChildren);
        }

        return children;
    }

    @Override
    public Result getMenuById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            return Result.error("菜单不存在");
        }
        return Result.success(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addMenu(SysMenu menu) {
        sysMenuMapper.insert(menu);
        return Result.success("新增菜单成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateMenu(SysMenu menu) {
        if (sysMenuMapper.selectById(menu.getId()) == null) {
            return Result.error("菜单不存在");
        }
        sysMenuMapper.updateById(menu);
        return Result.success("修改菜单成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteMenu(Long id) {
        if (sysMenuMapper.selectById(id) == null) {
            return Result.error("菜单不存在");
        }
        // 检查是否有子菜单
        LambdaQueryWrapper<SysMenu> childrenWrapper = new LambdaQueryWrapper<>();
        childrenWrapper.eq(SysMenu::getParentId, id);
        if (sysMenuMapper.selectCount(childrenWrapper) > 0) {
            return Result.error("存在子菜单，无法删除");
        }
        sysMenuMapper.deleteById(id);
        // 删除角色菜单关联
        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(SysRoleMenu::getMenuId, id);
        sysRoleMenuMapper.delete(roleMenuWrapper);
        return Result.success("删除菜单成功");
    }

    @Override
    public Result getMenusByRoleId(Long roleId) {
        // 查询该角色已分配的菜单ID
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(wrapper);
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        return Result.success(menuIds);
    }
}
