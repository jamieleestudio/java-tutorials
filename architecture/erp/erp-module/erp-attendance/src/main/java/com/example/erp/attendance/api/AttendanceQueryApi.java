package com.example.erp.attendance.api;

import com.example.erp.attendance.api.dto.AttendanceRecordDto;

import java.util.List;

public interface AttendanceQueryApi {

    AttendanceRecordDto findById(String id);

    List<AttendanceRecordDto> findByStudentId(String studentId);
}