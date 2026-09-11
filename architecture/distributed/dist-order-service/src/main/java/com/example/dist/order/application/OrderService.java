package com.example.dist.order.application;

import com.example.dist.order.application.command.CancelOrderCommand;
import com.example.dist.order.application.command.CreateOrderCommand;
import com.example.dist.order.application.command.PayOrderCommand;
import com.example.dist.order.application.dto.OrderDto;
import com.example.dist.order.application.query.GetOrderByIdQuery;
import com.example.dist.order.application.query.GetOrdersByCustomerQuery;

import java.util.List;

/**
 * Application service contract for the Order context.
 * Interfaces layer depends on this interface only, never on the impl.
 */
public interface OrderService {

    OrderDto createOrder(CreateOrderCommand command);

    void payOrder(PayOrderCommand command);

    void cancelOrder(CancelOrderCommand command);

    OrderDto getOrderById(GetOrderByIdQuery query);

    List<OrderDto> getOrdersByCustomer(GetOrdersByCustomerQuery query);
}