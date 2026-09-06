package com.example.eda.product.infrastructure.processed;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
}