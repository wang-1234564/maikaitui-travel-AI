package com.maikaitui.auth.controller;

import com.maikaitui.auth.entity.SysUser;
import com.maikaitui.auth.mapper.SysUserMapper;
import com.maikaitui.auth.service.AuthService;
import com.maikaitui.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return authService.login(username, password);
    }

    @PostMapping("/register")
    public Result register(@RequestBody SysUser user) {
        return authService.register(user);
    }

    @GetMapping("/userinfo")
    public Result userinfo(@RequestHeader("X-User-Id") Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return Result.error("User not found");
        Map<String, Object> data = Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "nickname", user.getNickname(),
            "phone", user.getPhone() != null ? user.getPhone() : "",
            "email", user.getEmail() != null ? user.getEmail() : "",
            "avatar", user.getAvatar() != null ? user.getAvatar() : ""
        );
        return Result.success(data);
    }

    @PostMapping("/refresh-token")
    public Result refreshToken(@RequestHeader("Authorization") String token) {
        String jwt = token;
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return authService.refreshToken(jwt);
    }

    @PutMapping("/profile")
    public Result updateProfile(@RequestBody SysUser user,
                                @RequestHeader("X-User-Id") Long userId) {
        user.setId(userId);
        return authService.updateProfile(user);
    }
}
