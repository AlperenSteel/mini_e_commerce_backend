package com.example.minicommerce.controller;

import com.example.minicommerce.dto.auth.AuthResponse;
import com.example.minicommerce.dto.auth.LoginRequest;
import com.example.minicommerce.dto.auth.RefreshRequest;
import com.example.minicommerce.dto.auth.RegisterRequest;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest,
                                 @RequestHeader("X-Device-Id") String deviceId) {
        return authService.register(registerRequest, deviceId);
    }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest,
                              @RequestHeader("X-Device-Id") String deviceId) {
        return authService.login(loginRequest, deviceId);
    }
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal User user,
                       @RequestHeader("X-Device-Id") String deviceId) {
        authService.logout(user, deviceId);
    }
    @PostMapping("/logout-all")
    public void logoutAll(@AuthenticationPrincipal User user) {
        authService.logoutAll(user);
    }
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest refreshRequest){
        return authService.refresh(refreshRequest);
    }
}
