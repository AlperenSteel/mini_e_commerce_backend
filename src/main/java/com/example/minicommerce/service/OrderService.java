package com.example.minicommerce.service;

import com.example.minicommerce.dto.OrderItemRequest;
import com.example.minicommerce.dto.OrderRequest;
import com.example.minicommerce.dto.OrderResponse;
import com.example.minicommerce.dto.OrderSummaryResponse;
import com.example.minicommerce.entity.Order;
import com.example.minicommerce.entity.OrderItem;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.enums.OrderStatus;
import com.example.minicommerce.exception.InsufficientStockException;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.OrderMapper;
import com.example.minicommerce.mapper.OrderSummaryMapper;
import com.example.minicommerce.repository.OrderRepository;
import com.example.minicommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.util.ArrayList;
import java.util.List;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderSummaryMapper orderSummaryMapper;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository, OrderMapper orderMapper, OrderSummaryMapper orderSummaryMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.orderSummaryMapper = orderSummaryMapper;
    }

    public OrderResponse create(OrderRequest orderRequest, User user) {
        Order order = new Order();
        order.setUser(user);

        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderItemRequest orderItems : orderRequest.getItems()) {
            Product product = productRepository.findById(orderItems.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bu product bulunamadı"));
            if(product.getStock() < orderItems.getQuantity()){
                throw new InsufficientStockException(product.getName() + " için yeterli stok yok" );
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItems.getQuantity());
            orderItem.setProduct(product);
            orderItem.setOrderPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItemList.add(orderItem);
            product.setStock(product.getStock() - orderItems.getQuantity());
            productRepository.save(product);
        }
        order.setItems(orderItemList);
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu id de Order yok"));
        return toOrderResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toOrderResponse);
    }

    public Page<OrderSummaryResponse> getUserOrders(User user, Pageable pageable) {

        return orderRepository.findAllByUser(user, pageable).map(this::toOrderSummaryResponse);
    }

    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu Order bulunamadı"));
        order.setStatus(status);
        return toOrderResponse(orderRepository.save(order));
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = orderMapper.toResponse(order);
        response.setTotalPrice(calculateTotal(order));
        return response;
    }
    private OrderSummaryResponse toOrderSummaryResponse(Order order){
        OrderSummaryResponse response = orderSummaryMapper.toResponse(order);
        response.setTotalPrice(calculateTotal(order));

        return response;
    }
    private double calculateTotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getOrderPrice() * item.getQuantity())
                .sum();
    }
}