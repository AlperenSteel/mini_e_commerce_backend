package com.example.minicommerce.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @Column(unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expireDate;

    private String deviceId;
}
