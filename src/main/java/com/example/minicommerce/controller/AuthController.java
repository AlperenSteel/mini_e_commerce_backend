package com.example.minicommerce.controller;

import com.example.minicommerce.dto.auth.AuthResponse;
import com.example.minicommerce.dto.auth.LoginRequest;
import com.example.minicommerce.dto.auth.RegisterRequest;
import com.example.minicommerce.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody  RegisterRequest registerRequest){

        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest){

        return authService.login(loginRequest);
    }

}
