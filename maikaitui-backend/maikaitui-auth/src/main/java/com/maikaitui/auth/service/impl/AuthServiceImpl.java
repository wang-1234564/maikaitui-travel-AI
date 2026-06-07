package com.maikaitui.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maikaitui.auth.entity.SysUser;
import com.maikaitui.auth.mapper.SysUserMapper;
import com.maikaitui.auth.service.AuthService;
import com.maikaitui.common.core.Result;
import com.maikaitui.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Result login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("Wrong password");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("Account disabled");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                Collections.emptyList());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());

        return Result.success(data);
    }

    @Override
    public Result register(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        Long count = sysUserMapper.selectCount(wrapper);

        if (count != null && count > 0) {
            return Result.error("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setDeleted(0);

        sysUserMapper.insert(user);

        return Result.success();
    }

    @Override
    public Result updateProfile(SysUser user) {
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            return Result.error("User not found");
        }
        if (user.getNickname() != null) {
            existing.setNickname(user.getNickname());
        }
        if (user.getPhone() != null) {
            existing.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            existing.setAvatar(user.getAvatar());
        }
        sysUserMapper.updateById(existing);
        return Result.success();
    }

    @Override
    public Result refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return Result.error("Invalid token");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String username = jwtTokenProvider.getUsername(token);

        String newToken = jwtTokenProvider.generateToken(userId, username, Collections.emptyList());

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);

        return Result.success(data);
    }
}
