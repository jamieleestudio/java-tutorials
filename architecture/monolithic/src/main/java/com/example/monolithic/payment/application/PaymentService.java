package com.example.monolithic.payment.application;

import com.example.monolithic.payment.application.command.CreatePaymentCommand;
import com.example.monolithic.payment.application.command.ProcessPaymentCommand;
import com.example.monolithic.payment.application.dto.PaymentDto;
import com.example.monolithic.payment.application.query.GetAllPaymentsQuery;
import com.example.monolithic.payment.application.query.GetPaymentByIdQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service contract for the Payment context.
 * Provider-defined interface — consumed by this context's interfaces layer
 * and by the Order context (cross-context calls).
 */
public interface PaymentService {

    PaymentDto createPayment(CreatePaymentCommand command);

    PaymentDto processPayment(ProcessPaymentCommand command);

    PaymentDto getPaymentById(GetPaymentByIdQuery query);

    List<PaymentDto> getAllPayments(GetAllPaymentsQuery query);

    // --- Cross-context methods (consumed by Order context) ---

    String pay(String orderId, BigDecimal amount);

    void refund(String paymentId);
}