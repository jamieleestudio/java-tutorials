package com.example.erp.attendance.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record")
public class AttendanceRecordEntity extends BaseEntity {

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }
}