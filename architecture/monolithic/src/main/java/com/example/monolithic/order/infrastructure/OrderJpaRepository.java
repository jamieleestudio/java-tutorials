package com.example.monolithic.order.infrastructure;

import com.example.monolithic.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for OrderEntity.
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByCustomerId(String customerId);
}