package com.example.minicommerce.controller;

import com.example.minicommerce.dto.UserResponse;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
        private final UserService userService;

        public UserController(UserService userService) {
            this.userService = userService;
        }

        @GetMapping("")
        public List<UserResponse> getAll() {
            return userService.getAll();
        }

        @GetMapping("/{id}")
        public UserResponse getById(@PathVariable Long id) {
            return userService.getById(id);
        }
    }

