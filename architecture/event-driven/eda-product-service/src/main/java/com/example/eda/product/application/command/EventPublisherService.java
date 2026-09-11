package com.example.eda.product.application.command;

import com.example.eda.shared.event.IntegrationEvent;

public interface EventPublisherService {

    void publish(IntegrationEvent event);
}