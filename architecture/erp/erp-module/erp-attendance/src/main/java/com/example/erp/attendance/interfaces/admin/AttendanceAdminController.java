package com.example.erp.attendance.interfaces.admin;

import com.example.erp.attendance.application.AttendanceApplicationService;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.admin.dto.AttendanceAdminResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/attendance")
public class AttendanceAdminController {

    private final AttendanceApplicationService attendanceApplicationService;

    public AttendanceAdminController(AttendanceApplicationService attendanceApplicationService) {
        this.attendanceApplicationService = attendanceApplicationService;
    }

    @GetMapping("/{id}")
    public AttendanceAdminResponse get(@PathVariable String id) {
        return toResponse(attendanceApplicationService.findById(id));
    }

    @GetMapping
    public List<AttendanceAdminResponse> byStudent(@RequestParam String studentId) {
        return attendanceApplicationService.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    private AttendanceAdminResponse toResponse(AttendanceRecordDto dto) {
        return new AttendanceAdminResponse(dto.id(), dto.studentId(), dto.status());
    }
}