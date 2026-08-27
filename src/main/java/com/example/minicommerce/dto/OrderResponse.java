package com.example.minicommerce.dto;

import com.example.minicommerce.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {

    private Long id;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private List<OrderItemResponse> items;
}
