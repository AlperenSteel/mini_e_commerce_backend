package com.example.minicommerce.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String jti, long remainingMillis) {
        if (remainingMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set("blacklist:" + jti, "true", Duration.ofMillis(remainingMillis));
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey("blacklist:" + jti);
    }
}