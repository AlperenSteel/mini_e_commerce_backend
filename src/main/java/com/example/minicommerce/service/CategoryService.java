package com.example.minicommerce.service;

import com.example.minicommerce.dto.CategoryRequest;
import com.example.minicommerce.dto.CategoryResponse;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.CategoryMapper;
import com.example.minicommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> getAllCategories(){
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }
    public CategoryResponse getById(Long id){
        return categoryMapper.toResponse(categoryRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı")));
    }
    public CategoryResponse create(CategoryRequest request){

        Category category = new Category();
        category.setName(request.getName());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }
    public void delete(Long id){
        categoryRepository.deleteById(id);
    }






}
