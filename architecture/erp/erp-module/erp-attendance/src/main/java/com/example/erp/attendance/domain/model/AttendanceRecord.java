package com.example.erp.attendance.domain.model;

import com.example.erp.attendance.domain.event.AttendanceClockedEvent;
import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

import java.time.LocalDateTime;

public class AttendanceRecord extends AggregateRoot {

    private static final int LATE_THRESHOLD_MINUTES = 10;

    private final String id;
    private final String studentId;
    private AttendanceStatus status;
    private LocalDateTime clockInTime;

    public AttendanceRecord(String id, String studentId, AttendanceStatus status, LocalDateTime clockInTime) {
        this.id = id;
        this.studentId = studentId;
        this.status = status;
        this.clockInTime = clockInTime;
    }

    public static AttendanceRecord clockIn(String id, String studentId, LocalDateTime clockInTime, LocalDateTime scheduleStart) {
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessRuleViolationException("studentId must not be blank");
        }
        if (clockInTime == null) {
            throw new BusinessRuleViolationException("clockInTime must not be null");
        }
        AttendanceStatus status = scheduleStart != null && clockInTime.isAfter(scheduleStart.plusMinutes(LATE_THRESHOLD_MINUTES))
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;
        AttendanceRecord record = new AttendanceRecord(id, studentId, status, clockInTime);
        record.registerEvent(new AttendanceClockedEvent(id, studentId, status.name(), clockInTime));
        return record;
    }

    public void markAbsent() {
        this.status = AttendanceStatus.ABSENT;
    }

    public String id() {
        return id;
    }

    public String studentId() {
        return studentId;
    }

    public AttendanceStatus status() {
        return status;
    }

    public LocalDateTime clockInTime() {
        return clockInTime;
    }
}