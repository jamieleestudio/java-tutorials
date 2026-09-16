package com.example.erp.attendance.interfaces.device.dto;

import java.time.LocalDateTime;

public record DeviceClockRequest(String studentId, LocalDateTime clockInTime, String faceToken) {
}