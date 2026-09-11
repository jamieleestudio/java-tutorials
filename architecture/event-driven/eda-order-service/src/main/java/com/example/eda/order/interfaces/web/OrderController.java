package com.example.eda.order.interfaces.web;

import com.example.eda.order.application.OrderService;
import com.example.eda.order.application.command.CreateOrderCommand;
import com.example.eda.order.application.dto.OrderDto;
import com.example.eda.order.application.query.GetOrderByIdQuery;
import com.example.eda.order.application.query.GetOrdersByCustomerQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Returns immediately after the local transaction (order + outbox).
     * Payment and inventory complete ASYNC — query status to observe the saga.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.lines().stream()
                        .map(line -> new CreateOrderCommand.OrderLine(line.productId(), line.quantity()))
                        .toList()
        );
        OrderDto dto = orderService.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(dto));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = (request == null || request.reason() == null || request.reason().isBlank())
                ? "No reason" : request.reason();
        orderService.cancelOrder(new com.example.eda.order.application.command.CancelOrderCommand(orderId, reason));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(OrderResponse.from(
                orderService.getOrderById(new GetOrderByIdQuery(orderId))));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@RequestParam String customerId) {
        return ResponseEntity.ok(orderService
                .getOrdersByCustomer(new GetOrdersByCustomerQuery(customerId)).stream()
                .map(OrderResponse::from)
                .toList());
    }
}