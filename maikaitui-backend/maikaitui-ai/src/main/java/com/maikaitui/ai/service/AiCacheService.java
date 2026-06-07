package com.maikaitui.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 回复内存缓存
 *
 * 问题:"黄山什么时候去最好" → 10人问 → 10次API调用 → 10次费用
 * 缓存后 → 首次调用API → 缓存24小时 → 后续命中缓存直接返回 → 省钱
 */
@Slf4j
@Service
public class AiCacheService {

    private static final long TTL_MILLIS = 24 * 60 * 60 * 1000L; // 24 小时

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final String reply;
        final long expireAt;
        CacheEntry(String reply, long expireAt) { this.reply = reply; this.expireAt = expireAt; }
    }

    /** 用 MD5 把问题转成短 key */
    private String hash(String question) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(question.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return "ai:" + sb;
        } catch (Exception e) {
            return "ai:" + question.hashCode();
        }
    }

    /** 查缓存，命中返回回复，未命中返回 null */
    public String get(String question) {
        String key = hash(question.trim().toLowerCase());
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireAt) {
            cache.remove(key);
            return null;
        }
        log.info("AI 缓存命中: {}", question.substring(0, Math.min(question.length(), 30)));
        return entry.reply;
    }

    /** 写入缓存 */
    public void put(String question, String reply) {
        String key = hash(question.trim().toLowerCase());
        cache.put(key, new CacheEntry(reply, System.currentTimeMillis() + TTL_MILLIS));
        // 超过 10000 条清理过期
        if (cache.size() > 10000) {
            cleanExpired();
        }
    }

    /** 清理过期条目 */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(e -> now > e.getValue().expireAt);
        log.info("缓存清理完成，当前 {} 条", cache.size());
    }
}
