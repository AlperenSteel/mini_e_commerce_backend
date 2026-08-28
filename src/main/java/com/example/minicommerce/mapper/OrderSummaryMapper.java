package com.example.minicommerce.mapper;


import com.example.minicommerce.dto.OrderItemResponse;
import com.example.minicommerce.dto.OrderSummaryResponse;
import com.example.minicommerce.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderSummaryMapper {
    OrderSummaryResponse toResponse(Order order);
}
