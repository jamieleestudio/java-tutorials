package com.example.erp.attendance.api.command;

import java.time.LocalDateTime;

public record ClockInCommand(String studentId, LocalDateTime clockInTime, String faceToken) {
}