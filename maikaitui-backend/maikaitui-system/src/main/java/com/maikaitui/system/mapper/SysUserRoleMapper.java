package com.maikaitui.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maikaitui.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
