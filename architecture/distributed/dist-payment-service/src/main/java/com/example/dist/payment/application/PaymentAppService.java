package com.example.dist.payment.application;

import com.example.dist.payment.application.command.CreatePaymentCommand;
import com.example.dist.payment.application.command.ProcessPaymentCommand;
import com.example.dist.payment.application.dto.PaymentDto;
import com.example.dist.payment.application.query.GetAllPaymentsQuery;
import com.example.dist.payment.application.query.GetPaymentByIdQuery;

import java.util.List;

/**
 * Payment OWN use-case contract — extends the cross-context {@link PaymentService}.
 * Injected by payment's own REST controllers; remote consumers only see the api module.
 */
public interface PaymentAppService extends PaymentService {

    PaymentDto createPayment(CreatePaymentCommand command);

    PaymentDto processPayment(ProcessPaymentCommand command);

    PaymentDto getPaymentById(GetPaymentByIdQuery query);

    List<PaymentDto> getAllPayments(GetAllPaymentsQuery query);
}