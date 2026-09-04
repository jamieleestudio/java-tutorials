package com.example.monolithic.payment.infrastructure;

import com.example.monolithic.payment.domain.Payment;
import com.example.monolithic.payment.domain.PaymentRepository;
import com.example.monolithic.payment.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
                payment.getId(), payment.getOrderId(), payment.getAmount(),
                payment.getStatus(), payment.getFailureReason()
        );
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return jpaRepository.findById(paymentId).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Payment toDomain(PaymentEntity entity) {
        return Payment.reconstitute(
                entity.getId(), entity.getOrderId(), entity.getAmount(),
                entity.getStatus(), entity.getFailureReason()
        );
    }
}