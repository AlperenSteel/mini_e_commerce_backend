package com.example.minicommerce.mapper;


import com.example.minicommerce.dto.ProductResponse;
import com.example.minicommerce.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = CategoryMapper.class)
public interface ProductMapper {
    ProductResponse toResponse(Product product);
    List<ProductResponse> toResponseList(List<Product> products);
}
