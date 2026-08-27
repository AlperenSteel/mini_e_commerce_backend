package com.example.minicommerce.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private int stock;
    private Double price;
    private CategoryResponse category;

}
