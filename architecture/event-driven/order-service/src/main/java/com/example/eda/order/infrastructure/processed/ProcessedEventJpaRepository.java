package com.example.eda.order.infrastructure.processed;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
}