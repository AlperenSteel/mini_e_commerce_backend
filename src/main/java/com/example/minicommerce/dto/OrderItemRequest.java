package com.example.minicommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemRequest {
    private Long productId;
    private int quantity;
}
