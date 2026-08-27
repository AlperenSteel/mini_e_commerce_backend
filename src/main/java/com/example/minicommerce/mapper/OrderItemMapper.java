package com.example.minicommerce.mapper;

import com.example.minicommerce.dto.OrderItemResponse;
import com.example.minicommerce.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = ProductMapper.class)
public interface OrderItemMapper {
    OrderItemResponse toResponse(OrderItem orderItem);
    List<OrderItemResponse> toResponseList(List<OrderItem> orderItemList);
}
