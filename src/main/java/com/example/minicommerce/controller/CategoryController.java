package com.example.minicommerce.controller;

import com.example.minicommerce.dto.CategoryRequest;
import com.example.minicommerce.dto.CategoryResponse;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }
    @GetMapping("")
    public List<CategoryResponse> getAllCategories(){
        return  categoryService.getAllCategories();

    }
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id){
        return categoryService.getById(id);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        categoryService.delete(id);
    }
    @PostMapping
    public CategoryResponse create(@RequestBody CategoryRequest category){
        return categoryService.create(category);
    }


}
