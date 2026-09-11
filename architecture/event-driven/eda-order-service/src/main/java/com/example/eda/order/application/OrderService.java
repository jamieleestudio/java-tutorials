package com.example.eda.order.application;

import com.example.eda.order.application.command.CancelOrderCommand;
import com.example.eda.order.application.command.CreateOrderCommand;
import com.example.eda.order.application.dto.OrderDto;
import com.example.eda.order.application.query.GetOrderByIdQuery;
import com.example.eda.order.application.query.GetOrdersByCustomerQuery;

import java.util.List;

/**
 * Order service contract — own use cases + saga reaction handlers
 * (invoked by Kafka consumers in the interfaces layer).
 */
public interface OrderService {

    OrderDto createOrder(CreateOrderCommand command);

    void cancelOrder(CancelOrderCommand command);

    OrderDto getOrderById(GetOrderByIdQuery query);

    List<OrderDto> getOrdersByCustomer(GetOrdersByCustomerQuery query);

    // --- Saga reaction handlers (payment-events / product-events) ---

    void onPaymentSucceeded(String eventId, String orderId, String paymentId);

    void onPaymentFailed(String eventId, String orderId, String reason);

    void onInventoryDeducted(String eventId, String orderId);

    void onInventoryDeductFailed(String eventId, String orderId, String reason);
}