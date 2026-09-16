package com.example.erp.attendance.interfaces.openapi.dto;

import java.time.LocalDateTime;

public record AttendanceOpenApiResponse(String recordId, String status, LocalDateTime clockInTime) {
}