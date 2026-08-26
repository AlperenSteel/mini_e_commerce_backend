package com.example.minicommerce.entity;


import com.example.minicommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    // Order oluştuturlunca order itemlar da oluşturulsun silinince onlar da silinsin diye yapıyoruz.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> items;

    //TODO ÖNEMLİ
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
