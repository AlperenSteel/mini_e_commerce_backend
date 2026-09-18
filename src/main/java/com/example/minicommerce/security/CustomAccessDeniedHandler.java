package com.example.minicommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String json = String.format(
                "{\"message\":\"%s\",\"status\":403,\"timestamp\":\"%s\"}",
                "Bu islem için yetkiniz yok ya da oturumunuz gecersiz",
                LocalDateTime.now()
        );

        response.setContentType("application/json");
        response.setStatus(403);
        response.getWriter().write(json);
    }
}