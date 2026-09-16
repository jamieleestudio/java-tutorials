package com.example.erp.attendance.domain.repository;

import com.example.erp.attendance.domain.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository {

    AttendanceRecord save(AttendanceRecord record);

    Optional<AttendanceRecord> findById(String id);

    List<AttendanceRecord> findByStudentId(String studentId);
}