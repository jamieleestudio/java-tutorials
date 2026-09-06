package com.example.cn.payment.infrastructure;

import com.example.cn.payment.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @Column(name = "payment_id")
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    public PaymentEntity(String id, String orderId, BigDecimal amount,
                         PaymentStatus status, String failureReason) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }
}