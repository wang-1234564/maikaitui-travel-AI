package com.maikaitui.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysUser;
import com.maikaitui.system.entity.SysUserRole;
import com.maikaitui.system.mapper.SysUserMapper;
import com.maikaitui.system.mapper.SysUserRoleMapper;
import com.maikaitui.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Result getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> result = sysUserMapper.selectPage(new Page<>(page, size), wrapper);
        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(result);
    }

    @Override
    public Result getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addUser(SysUser user) {
        // 校验用户名唯一
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        // 密码加密
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        sysUserMapper.insert(user);
        return Result.success("新增用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateUser(SysUser user) {
        SysUser existUser = sysUserMapper.selectById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        // 如果密码不为空，则加密新密码
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            user.setPassword(existUser.getPassword());
        }
        sysUserMapper.updateById(user);
        return Result.success("修改用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteUser(Long id) {
        if (sysUserMapper.selectById(id) == null) {
            return Result.error("用户不存在");
        }
        sysUserMapper.deleteById(id);
        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, id);
        sysUserRoleMapper.delete(wrapper);
        return Result.success("删除用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);
        // 插入新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                return userRole;
            }).collect(Collectors.toList());
            for (SysUserRole userRole : userRoles) {
                sysUserRoleMapper.insert(userRole);
            }
        }
        return Result.success("分配角色成功");
    }
}
