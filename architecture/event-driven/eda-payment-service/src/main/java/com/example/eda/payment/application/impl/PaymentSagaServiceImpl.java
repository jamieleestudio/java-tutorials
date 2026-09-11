package com.example.eda.payment.application.impl;

import com.example.eda.payment.application.PaymentSagaService;
import com.example.eda.payment.application.command.EventDeduplicationService;
import com.example.eda.payment.application.command.EventPublisherService;
import com.example.eda.payment.domain.Payment;
import com.example.eda.payment.domain.PaymentRepository;
import com.example.eda.shared.event.PaymentSucceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentSagaServiceImpl implements PaymentSagaService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final EventPublisherService eventPublisherService;
    private final EventDeduplicationService eventDeduplicationService;

    public PaymentSagaServiceImpl(PaymentRepository paymentRepository,
                                  EventPublisherService eventPublisherService,
                                  EventDeduplicationService eventDeduplicationService) {
        this.paymentRepository = paymentRepository;
        this.eventPublisherService = eventPublisherService;
        this.eventDeduplicationService = eventDeduplicationService;
    }

    @Override
    @Transactional
    public void processOrderPayment(String eventId, String orderId, BigDecimal amount) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        // Idempotency by orderId: duplicate OrderCreatedEvent → same payment
        Payment existing = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            eventDeduplicationService.markProcessed(eventId);
            return;
        }
        Payment payment = Payment.create(orderId, amount);
        payment.succeed(); // simulated gateway success
        paymentRepository.save(payment);

        // Same local tx: payment + outbox message
        eventPublisherService.publish(new PaymentSucceededEvent(orderId, payment.getId()));
        eventDeduplicationService.markProcessed(eventId);
        log.info("Payment succeeded for order {} (payment {})", orderId, payment.getId());
    }

    @Override
    @Transactional
    public void refundOrderPayment(String eventId, String orderId) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.refund(); // no-op unless SUCCESS — guard inside domain
            paymentRepository.save(payment);
            log.info("Refunded payment {} for cancelled order {}", payment.getId(), orderId);
        });
        eventDeduplicationService.markProcessed(eventId);
    }
}