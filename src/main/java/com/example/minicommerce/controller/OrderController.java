package com.example.minicommerce.controller;


import com.example.minicommerce.dto.OrderRequest;
import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.enums.OrderStatus;
import com.example.minicommerce.repository.OrderRepository;
import com.example.minicommerce.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("")
    public List<Order> getAllOrders(){
        return orderService.getAllOrders();
    }
    @GetMapping("/{id}")
    public Order getById(@PathVariable Long id){
        return orderService.getById(id);
    }
    @PostMapping("")
    public Order create(@RequestBody OrderRequest orderRequest){
        return orderService.create(orderRequest);
    }
    @GetMapping("/user")
    public List<Order> getUserOrder(User user){
        return orderService.getUserOrders(user);
    }
    @PatchMapping("{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody OrderStatus status){
        return orderService.updateStatus(id, status);
    }
}
