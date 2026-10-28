package com.yineng.bpe.rabbit.txmessage.controller;

import com.yineng.bpe.rabbit.txmessage.service.PaymentService;
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
        paymentService.pay(orderNo, userId, amount, points, false);
        return "支付成功：半消息落库 → 本地事务提交 → 提交半消息并投递";
    }

    @PostMapping("/fail")
    public String payFail(@RequestParam String orderNo, @RequestParam Long userId,
                          @RequestParam(defaultValue = "199.00") BigDecimal amount,
                          @RequestParam(defaultValue = "100") int points) {
        paymentService.pay(orderNo, userId, amount, points, true);
        return "本地事务失败：半消息已回滚，消息不会投递";
    }

    @PostMapping("/half")
    public String payHalf(@RequestParam String orderNo, @RequestParam Long userId,
                          @RequestParam(defaultValue = "100") int points) {
        paymentService.payHalfOnly(orderNo, userId, points);
        return "仅半消息落库（模拟执行业务前进程崩溃），30 秒后由回查任务裁决提交或回滚";
    }
}
