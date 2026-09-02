package com.example.minicommerce.entity;


import com.example.minicommerce.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
// İki tip constructor lazım biri:
// DB bir user satırını alıp fieldlerı (ilgili sütunları (ne kadar sütun var bilmiyor setter kullanacak))
// dönen objeye yerleştirirken kullanılacak --> Parametresiz constructor ----> Hibernate kullanıyor.
// Parametreli constructor'u biz kullanıyoruz kodda register endpoint'i ile olacak.
public class User extends BaseEntity {

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
}
