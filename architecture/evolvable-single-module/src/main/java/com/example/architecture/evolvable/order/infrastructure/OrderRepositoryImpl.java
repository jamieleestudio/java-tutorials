package com.example.architecture.evolvable.order.infrastructure;

import com.example.architecture.evolvable.order.domain.Order;
import com.example.architecture.evolvable.order.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = new OrderEntity(
            order.getId(),
            order.getProductName(),
            order.getAmount(),
            order.getStatus()
        );
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id)
            .map(entity -> new Order(
                entity.getId(),
                entity.getProductName(),
                entity.getAmount(),
                entity.getStatus()
            ));
    }
}
