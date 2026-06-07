package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;

public interface MiniappService {

    Result getHomeData();

    Result getUserStats(Long userId);

    Result recommend(String preference, int limit);
}
