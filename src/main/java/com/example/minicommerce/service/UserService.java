package com.example.minicommerce.service;


import com.example.minicommerce.dto.UserResponse;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.UserMapper;
import com.example.minicommerce.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    public UserResponse getById(Long id) {
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı")));
    }
    public List<UserResponse> getAll(){
        return userMapper.toResponseList(userRepository.findAll());
    }

}
