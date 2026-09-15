package com.example.minicommerce.repository;

import com.example.minicommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    // Bir method içerisinde @Transactional kullanılırsa ve o methodda
    // 2 kez db ye gidilirse her seferinde db ye gerçekten gidilsin diye
    // kullanılmadığında eski db den getirdiğini getirebilir
    // create de sorun olur
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity, " +
            "p.isActive = CASE WHEN (p.stock - :quantity) = 0 THEN false ELSE p.isActive END " +
            "WHERE p.id = :id AND p.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

}
