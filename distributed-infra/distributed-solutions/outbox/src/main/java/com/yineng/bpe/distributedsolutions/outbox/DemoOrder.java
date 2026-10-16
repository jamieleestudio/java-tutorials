package com.yineng.bpe.distributedsolutions.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 演示用业务表（订单）：与 outbox 记录在同一个事务中写入，用于展示原子性。
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "demo_orders")
public class DemoOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DemoOrder(String orderNo) {
        this.orderNo = orderNo;
        this.status = "CREATED";
        this.createdAt = LocalDateTime.now();
    }
}
