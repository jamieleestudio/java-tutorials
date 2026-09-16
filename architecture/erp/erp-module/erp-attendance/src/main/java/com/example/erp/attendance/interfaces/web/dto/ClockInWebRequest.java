package com.example.erp.attendance.interfaces.web.dto;

import java.time.LocalDateTime;

public record ClockInWebRequest(String studentId, LocalDateTime clockInTime, String faceToken) {
}