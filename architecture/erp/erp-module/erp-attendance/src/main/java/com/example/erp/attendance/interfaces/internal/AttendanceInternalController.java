package com.example.erp.attendance.interfaces.internal;

import com.example.erp.attendance.api.AttendanceQueryApi;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.internal.dto.AttendanceInternalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/attendance")
public class AttendanceInternalController {

    private final AttendanceQueryApi queryService;

    public AttendanceInternalController(AttendanceQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public AttendanceInternalResponse get(@PathVariable String id) {
        AttendanceRecordDto dto = queryService.findById(id);
        return new AttendanceInternalResponse(dto.id(), dto.studentId());
    }
}