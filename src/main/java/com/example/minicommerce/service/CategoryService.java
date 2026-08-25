package com.example.minicommerce.service;

import com.example.minicommerce.dto.CategoryRequest;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }
    public Category getById(Long id){
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
    }
    public Category create(CategoryRequest request){

        Category category = new Category();
        category.setName(request.getName());
        return categoryRepository.save(category);
    }
    public void delete(Long id){
        categoryRepository.deleteById(id);
    }






}
