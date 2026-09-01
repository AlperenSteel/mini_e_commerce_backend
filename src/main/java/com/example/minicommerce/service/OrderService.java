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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //TODO BURAYI ANLAT
    public OrderResponse create(OrderRequest orderRequest, User user) {
        Order order = new Order();
        order.setUser(user);

        Map<Long, Integer> productQuantityMap = new HashMap<>();
        for(OrderItemRequest item : orderRequest.getItems()){
            productQuantityMap.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        List<Product> updatedProducts = new ArrayList<>();

        for(Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()){
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bu product bulunamadı"));

            if(product.getStock() < quantity){
                throw new InsufficientStockException(product.getName() + " için yeterli stok yok");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(quantity);
            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setOrderPrice(product.getPrice());
            orderItemList.add(orderItem);
            product.setStock(product.getStock() - quantity);
            updatedProducts.add(product);
        }

        //TODO SOR: for'da her döngü içinde db ye product'u
        // güncellemektense bir sefer döngüden sonra tüm productları güncellemek?
        productRepository.saveAll(updatedProducts);
        order.setItems(orderItemList);
        order.setTotalPrice(calculateTotal(order));
        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu id de Order yok"));
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    public Page<OrderSummaryResponse> getUserOrders(User user, Pageable pageable) {
        return orderRepository.findAllByUser(user, pageable).map(orderSummaryMapper::toResponse);
    }

    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu Order bulunamadı"));
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private double calculateTotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getOrderPrice() * item.getQuantity())
                .sum();
    }
}}