package com.example.ms.payment.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Payment aggregate.
 */
public interface PaymentRepository {

    void save(Payment payment);

    Optional<Payment> findById(String paymentId);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findAll();
}