package com.example.erp.attendance.interfaces.provider;

import com.example.erp.attendance.api.AttendanceApi;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.application.AttendanceApplicationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttendanceApiProvider implements AttendanceApi {

    private final AttendanceApplicationService applicationService;

    public AttendanceApiProvider(AttendanceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public AttendanceRecordDto findById(String id) {
        return applicationService.findById(id);
    }

    @Override
    public List<AttendanceRecordDto> findByStudentId(String studentId) {
        return applicationService.findByStudentId(studentId);
    }

    @Override
    public AttendanceRecordDto clockIn(ClockInCommand command) {
        return applicationService.clockIn(command);
    }
}