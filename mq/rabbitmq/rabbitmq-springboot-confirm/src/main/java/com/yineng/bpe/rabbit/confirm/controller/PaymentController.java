package com.yineng.bpe.rabbit.confirm.controller;

import com.yineng.bpe.rabbit.confirm.producer.PointsProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    private final PointsProducer pointsProducer;

    public PaymentController(PointsProducer pointsProducer) {
        this.pointsProducer = pointsProducer;
    }

    @PostMapping
    public String pay(@RequestParam String orderNo, @RequestParam Long userId,
                      @RequestParam(defaultValue = "100") int points) {
        pointsProducer.sendPointsMessage(orderNo, userId, points);
        return "支付成功，积分消息已发送";
    }

    @PostMapping("/unroutable")
    public String payUnroutable(@RequestParam String orderNo, @RequestParam Long userId,
                                @RequestParam(defaultValue = "100") int points) {
        pointsProducer.sendUnroutableMessage(orderNo, userId, points);
        return "已发送不可路由消息，观察 Return 日志";
    }
}
