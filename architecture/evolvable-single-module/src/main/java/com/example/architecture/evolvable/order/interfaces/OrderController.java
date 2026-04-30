package com.example.architecture.evolvable.order.interfaces;

import com.example.architecture.evolvable.order.api.CreateOrderCommand;
import com.example.architecture.evolvable.order.api.OrderDTO;
import com.example.architecture.evolvable.order.application.OrderApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderApplicationService orderApp;

    public OrderController(OrderApplicationService orderApp) {
        this.orderApp = orderApp;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Validated @RequestBody CreateOrderCommand cmd) {
        OrderDTO order = orderApp.create(cmd);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable String id) {
        orderApp.payOrder(id);
        return ResponseEntity.ok().build();
    }
}
