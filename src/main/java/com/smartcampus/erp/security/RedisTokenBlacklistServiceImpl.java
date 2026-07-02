package com.smartcampus.erp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistServiceImpl implements TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklistServiceImpl.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Long> inMemoryBlacklist = new ConcurrentHashMap<>();

    public RedisTokenBlacklistServiceImpl(@Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token, long expirationTimeMs) {
        if (redisTemplate != null) {
            try {
                if (expirationTimeMs > 0) {
                    redisTemplate.opsForValue().set("jwt_blacklist:" + token, "blacklisted", expirationTimeMs, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("Failed to blacklist token in Redis: {}", e.getMessage());
            }
        } else {
            if (expirationTimeMs > 0) {
                inMemoryBlacklist.put(token, System.currentTimeMillis() + expirationTimeMs);
            }
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (redisTemplate != null) {
            try {
                Boolean hasKey = redisTemplate.hasKey("jwt_blacklist:" + token);
                return Boolean.TRUE.equals(hasKey);
            } catch (Exception e) {
                log.warn("Failed to check token blacklist in Redis (is Redis down?): {}", e.getMessage());
                return false;
            }
        } else {
            Long expiry = inMemoryBlacklist.get(token);
            if (expiry != null) {
                if (expiry > System.currentTimeMillis()) {
                    return true;
                } else {
                    inMemoryBlacklist.remove(token);
                }
            }
            return false;
        }
    }
}
