package com.example.minicommerce.repository;

import com.example.minicommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;



public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

}
