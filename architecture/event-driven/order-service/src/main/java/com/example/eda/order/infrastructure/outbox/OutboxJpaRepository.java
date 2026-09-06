package com.example.eda.order.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxMessageEntity, Long> {

    List<OutboxMessageEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}