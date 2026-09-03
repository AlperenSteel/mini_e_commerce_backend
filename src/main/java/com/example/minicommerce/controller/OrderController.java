package com.example.minicommerce.controller;


import com.example.minicommerce.dto.OrderRequest;
import com.example.minicommerce.dto.OrderResponse;
import com.example.minicommerce.dto.OrderSummaryResponse;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.enums.OrderStatus;
import com.example.minicommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("")
    public Page<OrderResponse> getAllOrders(Pageable pageable){
        return orderService.getAllOrders(pageable);
    }
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id, @AuthenticationPrincipal User user){
        return orderService.getById(id, user);
    }
    @PostMapping("")
    public OrderResponse create(@Valid @RequestBody OrderRequest orderRequest, @AuthenticationPrincipal User user){
        return orderService.create(orderRequest, user);
    }
    @GetMapping("/user")
    public Page<OrderSummaryResponse> getUserOrder(@AuthenticationPrincipal User user, Pageable pageable){
        return orderService.getUserOrders(user, pageable);
    }
    @PatchMapping("{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestBody OrderStatus status){
        return orderService.updateStatus(id, status);
    }
}
