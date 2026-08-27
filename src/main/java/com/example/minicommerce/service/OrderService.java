package com.example.minicommerce.service;


import com.example.minicommerce.dto.OrderItemRequest;
import com.example.minicommerce.dto.OrderRequest;
import com.example.minicommerce.dto.OrderResponse;
import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.OrderItem;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.enums.OrderStatus;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.OrderMapper;
import com.example.minicommerce.repository.OrderRepository;
import com.example.minicommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository, OrderMapper orderMapper){
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }
    public OrderResponse create(OrderRequest orderRequest, User user){

        Order order = new Order();
        order.setUser(user);
        // TODO SOR BURAYI
        List<OrderItem> orderItemlist = new ArrayList<>();
        for(OrderItemRequest orderItems: orderRequest.getItems()){
            Product product = productRepository.findById(orderItems.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bu product bulunamadı"));
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItems.getQuantity());
            orderItem.setProduct(product);
            orderItem.setOrderPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItemlist.add(orderItem);
        }
        order.setItems(orderItemlist);
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }
    public OrderResponse getById(Long id){
        return orderMapper.toResponse(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu id de Order yok")));
    }
    public Page<OrderResponse> getAllOrders(Pageable pageable){
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }
    public List<OrderResponse> getUserOrders(User user){
        return orderMapper.toResponseList(orderRepository.findAllByUser(user));
    }
    public OrderResponse updateStatus(Long id, OrderStatus status){
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bu Order bulunamadı"));
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }
}
