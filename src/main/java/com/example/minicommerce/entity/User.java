package com.example.minicommerce.entity;


import com.example.minicommerce.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
// İki tip constructor lazım biri:
// DB bir user satırını alıp fieldlerı (ilgili sütunları (ne kadar sütun var bilmiyor setter kullanacak))
// dönen objeye yerleştirirken kullanılacak --> Parametresiz constructor ----> Hibernate kullanıyor.
// Parametreli constructor'u biz kullanıyoruz kodda register endpoint'i ile olacak.
public class User extends BaseEntity implements UserDetails {

    // B-Tree for the indexing
    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String mail;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String username, String mail, String passwordHash, Role role){
        this.username = username;
        this.mail = mail;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
