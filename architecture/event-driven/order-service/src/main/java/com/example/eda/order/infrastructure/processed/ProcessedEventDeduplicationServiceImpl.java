package com.example.eda.order.infrastructure.processed;

import com.example.eda.order.application.command.EventDeduplicationService;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventDeduplicationServiceImpl implements EventDeduplicationService {

    private final ProcessedEventJpaRepository repository;

    public ProcessedEventDeduplicationServiceImpl(ProcessedEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isDuplicate(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        repository.save(new ProcessedEventEntity(eventId));
    }
}