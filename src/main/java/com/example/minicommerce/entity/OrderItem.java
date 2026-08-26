package com.example.minicommerce.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "orderitems")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends BaseEntity{


    private int quantity;

    private Double orderPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;



}
