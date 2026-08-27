package com.example.minicommerce.mapper;


import com.example.minicommerce.dto.OrderItemResponse;
import com.example.minicommerce.dto.OrderResponse;
import com.example.minicommerce.entity.Order;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    List<OrderResponse> toResponseList(List<Order> orders);
}
