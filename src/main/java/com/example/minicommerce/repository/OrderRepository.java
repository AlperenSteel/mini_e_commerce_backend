package com.example.minicommerce.repository;

import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;


import java.util.List;
//TODO
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser(User user);
    Page<Order> findAllByUser(User user, Pageable pageable);
}
