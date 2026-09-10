package com.example.minicommerce.service;

import com.example.minicommerce.dto.ProductRequest;
import com.example.minicommerce.dto.ProductResponse;
import com.example.minicommerce.entity.Category;
import com.example.minicommerce.entity.Product;
import com.example.minicommerce.exception.ResourceNotFoundException;
import com.example.minicommerce.mapper.ProductMapper;
import com.example.minicommerce.repository.CategoryRepository;
import com.example.minicommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı"));

        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Ürün bulunamadı");
        }

        return productMapper.toResponse(product);
    }
    public PagedModel<ProductResponse> getAllProducts(Pageable pageable){
        Page<ProductResponse> page = productRepository.findAllByIsActiveTrue(pageable).map(productMapper::toResponse);
        return new PagedModel<>(page);
    }
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }
    public ProductResponse update(Long id, ProductRequest productRequest){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product bulunamadı"));
        product.setName(productRequest.getName());
        product.setStock(productRequest.getStock());
        product.setStock(productRequest.getStock());
        if (productRequest.getStock() > 0) {
            product.setIsActive(true);
        }
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı"));
        product.setCategory(category);
        return productMapper.toResponse(productRepository.save(product));
    }

}
