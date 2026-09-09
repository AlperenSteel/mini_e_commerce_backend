package com.example.minicommerce.repository;


import com.example.minicommerce.entity.RefreshToken;
import com.example.minicommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteAllByUser(User user);
    void deleteAllByExpireDateBefore(LocalDateTime date);
}
