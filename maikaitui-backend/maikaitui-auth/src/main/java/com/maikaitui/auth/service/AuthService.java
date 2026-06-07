package com.maikaitui.auth.service;

import com.maikaitui.auth.entity.SysUser;
import com.maikaitui.common.core.Result;

public interface AuthService {

    Result login(String username, String password);

    Result register(SysUser user);

    Result refreshToken(String token);

    Result updateProfile(SysUser user);
}
