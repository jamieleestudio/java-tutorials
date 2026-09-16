package com.example.erp.attendance.interfaces.mobile.dto;

import java.time.LocalDateTime;

public record MobileClockInRequest(String studentId, LocalDateTime clockInTime, String faceToken) {
}