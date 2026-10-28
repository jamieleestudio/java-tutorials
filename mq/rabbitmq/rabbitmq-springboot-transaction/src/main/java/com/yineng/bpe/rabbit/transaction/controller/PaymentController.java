package com.yineng.bpe.rabbit.transaction.controller;

import com.yineng.bpe.rabbit.transaction.producer.PointsTxProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    private final PointsTxProducer pointsTxProducer;

    public PaymentController(PointsTxProducer pointsTxProducer) {
        this.pointsTxProducer = pointsTxProducer;
    }

    @PostMapping
    public String pay(@RequestParam String orderNo, @RequestParam Long userId,
                      @RequestParam(defaultValue = "100") int points)
            throws IOException {
        pointsTxProducer.sendInTransaction(orderNo, userId, points);
        return "支付成功，事务提交，消息已确认";
    }

    @PostMapping("/fail")
    public String payFail(@RequestParam String orderNo, @RequestParam Long userId,
                          @RequestParam(defaultValue = "100") int points)
            throws IOException {
        pointsTxProducer.sendThenFail(orderNo, userId, points);
        return "业务失败，事务已回滚，观察消费者不会收到消息";
    }
}
