package com.xy.service;

import com.xy.util.chat.Result;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天缓存服务，用于缓存近期请求和响应，减少重复请求
 */
@Service
public class ChatCacheService {
    
    // 缓存的最大容量
    private static final int MAX_CACHE_SIZE = 100;
    
    // 缓存的最大存活时间（10分钟）
    private static final long MAX_CACHE_AGE_MS = 10 * 60 * 1000;
    
    // 缓存映射：用户ID + 消息哈希 -> 缓存项
    private final Map<String, CacheItem> responseCache = new ConcurrentHashMap<>();
    
    // 活跃会话映射：用户ID -> 会话信息
    private final Map<String, Map<String, String>> activeSessionsCache = new ConcurrentHashMap<>();
    
    /**
     * 获取缓存的结果
     * @param userId 用户ID
     * @param message 消息内容
     * @return 缓存的结果，如果没有缓存则返回null
     */
    public Result getCachedResult(String userId, String message) {
        String key = generateCacheKey(userId, message);
        CacheItem cacheItem = responseCache.get(key);
        
        if (cacheItem != null && !isCacheExpired(cacheItem)) {
            // 如果缓存存在且未过期，更新访问时间并返回结果
            cacheItem.setLastAccessTime(Instant.now().toEpochMilli());
            return cacheItem.getResult();
        }
        
        return null;
    }
    
    /**
     * 将结果存入缓存
     * @param userId 用户ID
     * @param message 消息内容
     * @param result 结果对象
     */
    public void cacheResult(String userId, String message, Result result) {
        // 如果缓存已满，清理最老的缓存
        if (responseCache.size() >= MAX_CACHE_SIZE) {
            cleanOldestCache();
        }
        
        String key = generateCacheKey(userId, message);
        long now = Instant.now().toEpochMilli();
        responseCache.put(key, new CacheItem(result, now, now));
    }
    
    /**
     * 获取用户的会话信息
     * @param userId 用户ID
     * @return 会话信息，如果没有则返回null
     */
    public Map<String, String> getSessionInfo(String userId) {
        return activeSessionsCache.get(userId);
    }
    
    /**
     * 保存用户的会话信息
     * @param userId 用户ID
     * @param sessionInfo 会话信息
     */
    public void saveSessionInfo(String userId, Map<String, String> sessionInfo) {
        activeSessionsCache.put(userId, sessionInfo);
    }
    
    /**
     * 移除用户的会话信息
     * @param userId 用户ID
     */
    public void removeSessionInfo(String userId) {
        activeSessionsCache.remove(userId);
    }
    
    /**
     * 清理用户的所有缓存
     * @param userId 用户ID
     */
    public void clearUserCache(String userId) {
        responseCache.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        activeSessionsCache.remove(userId);
    }
    
    /**
     * 生成缓存键
     * @param userId 用户ID
     * @param message 消息内容
     * @return 缓存键
     */
    private String generateCacheKey(String userId, String message) {
        // 使用简单的哈希避免过长的键
        int messageHash = message.hashCode();
        return userId + ":" + messageHash;
    }
    
    /**
     * 检查缓存是否过期
     * @param cacheItem 缓存项
     * @return 是否过期
     */
    private boolean isCacheExpired(CacheItem cacheItem) {
        long now = Instant.now().toEpochMilli();
        return (now - cacheItem.getCreationTime()) > MAX_CACHE_AGE_MS;
    }
    
    /**
     * 清理最老的缓存项
     */
    private void cleanOldestCache() {
        if (responseCache.isEmpty()) return;
        
        // 查找最老的缓存项
        String oldestKey = null;
        long oldestAccessTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, CacheItem> entry : responseCache.entrySet()) {
            if (entry.getValue().getLastAccessTime() < oldestAccessTime) {
                oldestAccessTime = entry.getValue().getLastAccessTime();
                oldestKey = entry.getKey();
            }
        }
        
        // 删除最老的缓存项
        if (oldestKey != null) {
            responseCache.remove(oldestKey);
        }
    }
    
    /**
     * 缓存项内部类
     */
    private static class CacheItem {
        private final Result result;
        private final long creationTime;
        private long lastAccessTime;
        
        public CacheItem(Result result, long creationTime, long lastAccessTime) {
            this.result = result;
            this.creationTime = creationTime;
            this.lastAccessTime = lastAccessTime;
        }
        
        public Result getResult() {
            return result;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }
    }
} 