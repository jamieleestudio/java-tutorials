package com.example.monolithic.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, String> {

    Optional<PaymentEntity> findByOrderId(String orderId);
}