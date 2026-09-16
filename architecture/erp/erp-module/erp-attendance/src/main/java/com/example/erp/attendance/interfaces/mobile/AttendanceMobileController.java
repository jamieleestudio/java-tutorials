package com.example.erp.attendance.interfaces.mobile;

import com.example.erp.attendance.api.AttendanceClockApi;
import com.example.erp.attendance.api.AttendanceQueryApi;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.mobile.dto.AttendanceMobileResponse;
import com.example.erp.attendance.interfaces.mobile.dto.MobileClockInRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile/attendance")
public class AttendanceMobileController {

    private final AttendanceQueryApi queryService;
    private final AttendanceClockApi clockService;

    public AttendanceMobileController(AttendanceQueryApi queryService, AttendanceClockApi clockService) {
        this.queryService = queryService;
        this.clockService = clockService;
    }

    @GetMapping("/{id}")
    public AttendanceMobileResponse get(@PathVariable String id) {
        return toResponse(queryService.findById(id));
    }

    @PostMapping("/clock-in")
    public AttendanceMobileResponse clockIn(@RequestBody MobileClockInRequest request) {
        return toResponse(clockService.clockIn(new ClockInCommand(request.studentId(), request.clockInTime(), request.faceToken())));
    }

    private AttendanceMobileResponse toResponse(AttendanceRecordDto dto) {
        return new AttendanceMobileResponse(dto.id(), dto.status());
    }
}