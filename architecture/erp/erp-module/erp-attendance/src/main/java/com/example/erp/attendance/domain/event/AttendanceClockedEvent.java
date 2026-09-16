package com.example.erp.attendance.domain.event;

import com.example.erp.shared.DomainEvent;

import java.time.LocalDateTime;

public record AttendanceClockedEvent(String recordId, String studentId, String status, LocalDateTime occurredAt)
        implements DomainEvent {
}