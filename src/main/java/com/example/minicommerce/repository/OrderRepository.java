package com.example.minicommerce.repository;

import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser(User user);
}
