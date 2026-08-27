package com.example.minicommerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemRequest {

    @NotNull
    private Long productId;

    @Positive
    private int quantity;
}
