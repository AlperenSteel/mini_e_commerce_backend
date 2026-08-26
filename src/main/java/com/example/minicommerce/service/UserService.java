package com.example.minicommerce.service;


import com.example.minicommerce.entity.User;
import com.example.minicommerce.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }
    
}
