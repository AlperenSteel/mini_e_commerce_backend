package com.example.minicommerce.service;

import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.dto.ProductResponse;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.ProductMapper;
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
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse create(ProductRequest productRequest){
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        Category category = categoryRepository.findById(productRequest.getCategoryId()).
                orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı. "));
        product.setCategory(category);
        return productMapper.toResponse(productRepository.save(product));
    }
    public ProductResponse getById(Long id){
        return productMapper.toResponse(productRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Bu id'ye sahioProduct bulunamadı")));
    }
    public List<ProductResponse> getAllProducts(){
        return productMapper.toResponseList(productRepository.findAll());
    }
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }

}
