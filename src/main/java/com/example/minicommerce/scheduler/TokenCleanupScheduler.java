package com.example.minicommerce.scheduler;


import com.example.minicommerce.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanExpiredToken(){
        refreshTokenRepository.deleteAllByExpireDateBefore(LocalDateTime.now());
    }

}
