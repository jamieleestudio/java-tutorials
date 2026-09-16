package com.example.erp.attendance.interfaces.internal;

import com.example.erp.attendance.application.AttendanceApplicationService;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.internal.dto.AttendanceInternalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/attendance")
public class AttendanceInternalController {

    private final AttendanceApplicationService attendanceApplicationService;

    public AttendanceInternalController(AttendanceApplicationService attendanceApplicationService) {
        this.attendanceApplicationService = attendanceApplicationService;
    }

    @GetMapping("/{id}")
    public AttendanceInternalResponse get(@PathVariable String id) {
        AttendanceRecordDto dto = attendanceApplicationService.findById(id);
        return new AttendanceInternalResponse(dto.id(), dto.studentId());
    }
}