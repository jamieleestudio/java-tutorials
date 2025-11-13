package com.yineng.bpe.rpc.order.controller;

import com.yineng.bpe.rpc.order.api.OrderService;
import com.yineng.bpe.rpc.order.controller.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    @Reference
    private OrderService orderService;

    @GetMapping("/list")
    public List<String> findOrderList(){
        return orderService.findOrderList();
    }

}
