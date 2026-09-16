package com.example.erp.attendance.api;

import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;

public interface AttendanceClockApi {

    AttendanceRecordDto clockIn(ClockInCommand command);
}