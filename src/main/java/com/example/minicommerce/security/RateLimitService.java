package com.example.minicommerce.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitService(RedisTemplate<String,String> redisTemplate){
        this.redisTemplate = redisTemplate;

    }
    public boolean tryAcquireLoginLock(String username){
        Boolean acquire = redisTemplate
                .opsForValue().setIfAbsent("login-lock:" + username, "1", Duration.ofSeconds(3));
        return Boolean.TRUE.equals(acquire);
    }
    public void releaseLoginLock(String username){
        redisTemplate.delete("login-lock:" + username);

    }
}
