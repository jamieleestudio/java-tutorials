package com.example.eda.order.infrastructure;

import com.example.eda.order.domain.OrderStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "payment_succeeded", nullable = false)
    private boolean paymentSucceeded;

    @Column(name = "inventory_deducted", nullable = false)
    private boolean inventoryDeducted;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    public OrderEntity(String id, String customerId, OrderStatus status, String paymentId,
                       boolean paymentSucceeded, boolean inventoryDeducted,
                       List<OrderItemEmbeddable> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.paymentId = paymentId;
        this.paymentSucceeded = paymentSucceeded;
        this.inventoryDeducted = inventoryDeducted;
        this.items = items;
    }
}