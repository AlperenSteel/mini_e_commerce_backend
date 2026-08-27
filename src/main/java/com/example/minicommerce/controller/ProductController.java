package com.example.minicommerce.controller;


import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.dto.ProductResponse;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    // PAGEABLE gerekli çünkü ----> tamamını getirme performans kötü olur
    @GetMapping("")
    public Page<ProductResponse> getAllProducts(Pageable pageable){
        return productService.getAllProducts(pageable);
    }
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id){
        return productService.getById(id);
    }
    @PostMapping("")
    public ProductResponse create(@Valid @RequestBody ProductRequest product){
        return productService.create(product);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        productService.deleteProduct(id);
    }

}
