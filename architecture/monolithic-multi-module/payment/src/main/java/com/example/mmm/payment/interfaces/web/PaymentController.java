package com.example.mmm.payment.interfaces.web;

import com.example.mmm.payment.application.PaymentService;
import com.example.mmm.payment.application.command.CreatePaymentCommand;
import com.example.mmm.payment.application.command.ProcessPaymentCommand;
import com.example.mmm.payment.application.dto.PaymentDto;
import com.example.mmm.payment.application.query.GetAllPaymentsQuery;
import com.example.mmm.payment.application.query.GetPaymentByIdQuery;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CreatePaymentCommand command = new CreatePaymentCommand(request.orderId(), request.amount());
        PaymentDto dto = paymentService.createPayment(command);
        return ResponseEntity.ok(PaymentResponse.from(dto));
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String paymentId) {
        PaymentDto dto = paymentService.processPayment(new ProcessPaymentCommand(paymentId));
        return ResponseEntity.ok(PaymentResponse.from(dto));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        PaymentDto dto = paymentService.getPaymentById(new GetPaymentByIdQuery(paymentId));
        return ResponseEntity.ok(PaymentResponse.from(dto));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> responses = paymentService.getAllPayments(new GetAllPaymentsQuery()).stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}