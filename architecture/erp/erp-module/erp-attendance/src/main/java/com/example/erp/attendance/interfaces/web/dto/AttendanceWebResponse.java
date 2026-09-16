package com.example.erp.attendance.interfaces.web.dto;

import java.time.LocalDateTime;

public record AttendanceWebResponse(String id, String studentId, String status, LocalDateTime clockInTime) {
}