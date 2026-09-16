package com.example.erp.shared;

import java.time.LocalDateTime;

public interface DomainEvent {

    LocalDateTime occurredAt();
}