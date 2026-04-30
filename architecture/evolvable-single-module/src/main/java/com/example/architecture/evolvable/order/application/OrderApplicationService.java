package com.example.architecture.evolvable.order.application;

import com.example.architecture.evolvable.order.api.CreateOrderCommand;
import com.example.architecture.evolvable.order.api.OrderDTO;
import com.example.architecture.evolvable.order.domain.Order;
import com.example.architecture.evolvable.order.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service orchestrates use cases.
 * Crosses module boundaries via API contracts if needed.
 */
@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDTO create(CreateOrderCommand cmd) {
        Order order = Order.create(cmd.productName(), cmd.amount());
        orderRepository.save(order);
        return new OrderDTO(order.getId(), order.getProductName(), order.getAmount(), order.getStatus());
    }
    
    @Transactional
    public void payOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.pay();
        orderRepository.save(order);
    }
}
