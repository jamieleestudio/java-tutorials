package com.example.mmm.order.interfaces.web;

import com.example.mmm.order.application.OrderService;
import com.example.mmm.order.application.command.CancelOrderCommand;
import com.example.mmm.order.application.command.CreateOrderCommand;
import com.example.mmm.order.application.command.PayOrderCommand;
import com.example.mmm.order.application.dto.OrderDto;
import com.example.mmm.order.application.query.GetOrderByIdQuery;
import com.example.mmm.order.application.query.GetOrdersByCustomerQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order operations.
 * Lives in the interfaces layer.
 * Calls the application service interface only — never impl, domain or infrastructure directly.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

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

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable String orderId) {
        orderService.payOrder(new PayOrderCommand(orderId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = (request == null || request.reason() == null || request.reason().isBlank())
                ? "No reason" : request.reason();
        orderService.cancelOrder(new CancelOrderCommand(orderId, reason));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        OrderDto dto = orderService.getOrderById(new GetOrderByIdQuery(orderId));
        return ResponseEntity.ok(OrderResponse.from(dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@RequestParam String customerId) {
        List<OrderResponse> responses = orderService
                .getOrdersByCustomer(new GetOrdersByCustomerQuery(customerId)).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}