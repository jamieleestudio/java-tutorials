package com.example.cn.payment.application;

import com.example.cn.payment.application.command.CreatePaymentCommand;
import com.example.cn.payment.application.command.ProcessPaymentCommand;
import com.example.cn.payment.application.dto.PaymentDto;
import com.example.cn.payment.application.query.GetAllPaymentsQuery;
import com.example.cn.payment.application.query.GetPaymentByIdQuery;

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