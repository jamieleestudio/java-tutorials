package com.example.erp.attendance.api.dto;

import java.time.LocalDateTime;

public record AttendanceRecordDto(String id, String studentId, String status, LocalDateTime clockInTime) {
}