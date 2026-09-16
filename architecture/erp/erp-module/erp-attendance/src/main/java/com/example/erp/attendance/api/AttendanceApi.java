package com.example.erp.attendance.api;

import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;

import java.util.List;

public interface AttendanceApi {

    AttendanceRecordDto findById(String id);

    List<AttendanceRecordDto> findByStudentId(String studentId);

    AttendanceRecordDto clockIn(ClockInCommand command);
}
