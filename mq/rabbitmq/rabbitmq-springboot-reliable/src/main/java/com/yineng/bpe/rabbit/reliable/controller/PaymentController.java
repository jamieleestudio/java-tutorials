package com.yineng.bpe.rabbit.reliable.controller;

import com.yineng.bpe.rabbit.reliable.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public String pay(@RequestParam String orderNo, @RequestParam Long userId,
                      @RequestParam(defaultValue = "199.00") BigDecimal amount,
                      @RequestParam(defaultValue = "100") int points) {
        paymentService.pay(orderNo, userId, amount, points);
        return "支付成功：业务与消息同一事务落库，事务提交后异步发送，Confirm 确认后更新状态";
    }
}
