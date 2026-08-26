package com.example.minicommerce.service;

import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.repository.CategoryRepository;
import com.example.minicommerce.repository.ProductRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product create(ProductRequest productRequest){
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        Category category = categoryRepository.findById(productRequest.getCategoryId()).
                orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı. "));
        product.setCategory(category);
        return productRepository.save(product);
    }
    public Product getById(Long id){
        return productRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Bu id'ye sahioProduct bulunamadı"));
    }
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }

}
