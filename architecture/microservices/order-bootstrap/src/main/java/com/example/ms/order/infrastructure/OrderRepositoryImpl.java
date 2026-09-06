package com.example.ms.order.infrastructure;

import com.example.ms.order.domain.Order;
import com.example.ms.order.domain.OrderItem;
import com.example.ms.order.domain.OrderRepository;
import com.example.ms.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderRepository port using JPA.
 * This belongs to infrastructure — it depends on domain (implements its interface),
 * not the other way around.
 */
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        List<OrderItemEmbeddable> itemEntities = order.getItems().stream()
                .map(item -> new OrderItemEmbeddable(
                        item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice()))
                .toList();

        OrderEntity entity = new OrderEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getPaymentId(),
                itemEntities
        );
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return jpaRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(this::toDomain)
                .toList();
    }

    private Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(embeddable -> new OrderItem(
                        embeddable.getProductId(),
                        embeddable.getProductName(),
                        embeddable.getQuantity(),
                        embeddable.getUnitPrice()))
                .toList();

        return Order.reconstitute(
                entity.getId(),
                entity.getCustomerId(),
                items,
                entity.getStatus(),
                entity.getPaymentId()
        );
    }
}