package com.example.minicommerce.mapper;


import com.example.minicommerce.dto.CategoryResponse;
import com.example.minicommerce.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categoryList);
}
