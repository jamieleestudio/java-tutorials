package com.example.mmm.order.infrastructure;

import com.example.mmm.order.application.command.EventPublisherService;
import com.example.mmm.shared.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link EventPublisherService} using Spring's ApplicationEventPublisher.
 */
@Component
public class SpringEventPublisherServiceImpl implements EventPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherServiceImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}