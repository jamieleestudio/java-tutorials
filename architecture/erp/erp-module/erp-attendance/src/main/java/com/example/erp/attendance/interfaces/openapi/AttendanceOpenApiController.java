package com.example.erp.attendance.interfaces.openapi;

import com.example.erp.attendance.api.AttendanceQueryApi;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.openapi.dto.AttendanceOpenApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/openapi/v1/attendance")
public class AttendanceOpenApiController {

    private final AttendanceQueryApi queryService;

    public AttendanceOpenApiController(AttendanceQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public AttendanceOpenApiResponse get(@PathVariable String id) {
        return toResponse(queryService.findById(id));
    }

    @GetMapping
    public List<AttendanceOpenApiResponse> byStudent(@RequestParam String studentId) {
        return queryService.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    private AttendanceOpenApiResponse toResponse(AttendanceRecordDto dto) {
        return new AttendanceOpenApiResponse(dto.id(), dto.status(), dto.clockInTime());
    }
}