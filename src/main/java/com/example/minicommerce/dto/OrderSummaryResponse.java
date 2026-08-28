package com.example.minicommerce.dto;
import com.example.minicommerce.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderSummaryResponse {

        private Long id;
        private LocalDateTime createdAt;
        private OrderStatus status;

        private Double totalPrice;
    }
