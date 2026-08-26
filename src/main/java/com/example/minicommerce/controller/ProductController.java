package com.example.minicommerce.controller;


import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }
    @GetMapping("")
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id){
        return productService.getById(id);
    }
    @PostMapping("")
    public Product create(@RequestBody ProductRequest product){
        return productService.create(product);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        productService.deleteProduct(id);
    }

}
