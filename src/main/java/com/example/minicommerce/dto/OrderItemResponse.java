package com.example.minicommerce.dto;


import com.example.minicommerce.dto.ProductResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private int quantity;
    private Double orderPrice;
    private ProductResponse product;
}
