package com.example.dist.payment.application.impl;

import com.example.dist.payment.application.PaymentAppService;
import com.example.dist.payment.application.PaymentService;
import com.example.dist.payment.application.command.CreatePaymentCommand;
import com.example.dist.payment.application.command.ProcessPaymentCommand;
import com.example.dist.payment.application.dto.PaymentDto;
import com.example.dist.payment.application.query.GetAllPaymentsQuery;
import com.example.dist.payment.application.query.GetPaymentByIdQuery;
import com.example.dist.payment.domain.Payment;
import com.example.dist.payment.domain.PaymentRepository;
import com.example.dist.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of {@link PaymentAppService} (which extends the cross-context PaymentService).
 */
@Service
public class PaymentServiceImpl implements PaymentAppService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentDto createPayment(CreatePaymentCommand command) {
        Payment payment = Payment.create(command.orderId(), command.amount());
        paymentRepository.save(payment);
        return toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto processPayment(ProcessPaymentCommand command) {
        Payment payment = findPaymentOrThrow(command.paymentId());
        // Simulate payment processing — always succeeds in demo
        payment.succeed();
        paymentRepository.save(payment);
        return toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(GetPaymentByIdQuery query) {
        return toDto(findPaymentOrThrow(query.paymentId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments(GetAllPaymentsQuery query) {
        return paymentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public String pay(String orderId, BigDecimal amount) {
        // Distributed idempotency: RPC retries must not create duplicate payments.
        // Same order → same payment ID (orderId uniqueness + state check).
        Payment existing = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            return existing.getId();
        }
        Payment payment = Payment.create(orderId, amount);
        // Simulate successful payment
        payment.succeed();
        paymentRepository.save(payment);
        return payment.getId();
    }

    @Override
    @Transactional
    public void refund(String paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        payment.refund();
        paymentRepository.save(payment);
    }

    private Payment findPaymentOrThrow(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("PAYMENT_NOT_FOUND",
                        "Payment not found: " + paymentId));
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getOrderId(),
                payment.getAmount(), payment.getStatus().name());
    }
}