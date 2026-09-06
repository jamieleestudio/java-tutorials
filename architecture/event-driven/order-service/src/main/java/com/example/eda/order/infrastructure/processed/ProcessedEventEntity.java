package com.example.eda.order.infrastructure.processed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Consumer-side idempotency: eventId unique — duplicate deliveries are skipped.
 */
@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor
public class ProcessedEventEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedEventEntity(String eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }
}