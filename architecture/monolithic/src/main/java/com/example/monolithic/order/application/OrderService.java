package com.example.monolithic.order.application;

import com.example.monolithic.order.application.command.CancelOrderCommand;
import com.example.monolithic.order.application.command.CreateOrderCommand;
import com.example.monolithic.order.application.command.PayOrderCommand;
import com.example.monolithic.order.application.dto.OrderDto;
import com.example.monolithic.order.application.query.GetOrderByIdQuery;
import com.example.monolithic.order.application.query.GetOrdersByCustomerQuery;

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