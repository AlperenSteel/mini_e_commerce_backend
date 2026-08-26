package com.example.minicommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity{


    private String name;

    private String description;

    private int stock;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
