package com.example.minicommerce.service;


import com.example.minicommerce.dto.OrderItemRequest;
import com.example.minicommerce.dto.OrderRequest;
import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.OrderItem;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.repository.OrderRepository;
import com.example.minicommerce.repository.ProductRepository;
import com.example.minicommerce.repository.UserRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        ProductRepository productRepository){
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    public Order create(OrderRequest orderRequest, User user){

        Order order = new Order();
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest itemRequest : orderRequest.getItems()){
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Bu product bulunamadı."));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setOrderPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        orderRepository.save(order);
        return order;
    }
    public Order getById(Long id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bu id de Order yok"));
    }
    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }
    public List<Order> getUserOrders(User user){
        return orderRepository.findAllByUser(user);
    }
    



}
