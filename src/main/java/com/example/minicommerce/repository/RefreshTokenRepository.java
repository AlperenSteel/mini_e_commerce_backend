package com.example.minicommerce.repository;


import com.example.minicommerce.entity.RefreshToken;
import com.example.minicommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteAllByUser(User user);                              // logout-all için kullanılacak
    void deleteAllByExpireDateBefore(LocalDateTime date);
    void deleteByUserAndDeviceId(User user, String deviceId);      // tekil logout/login için

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO refresh_tokens (user_id, device_id, token, expire_date) " +
            "VALUES (:userId, :deviceId, :token, :expireDate) " +
            "ON CONFLICT (user_id, device_id) " +
            "DO UPDATE SET token = :token, expire_date = :expireDate",
            nativeQuery = true)
    void upsertRefreshToken(@Param("userId") Long userId, @Param("deviceId") String deviceId,
                            @Param("token") String token, @Param("expireDate") LocalDateTime expireDate);
}