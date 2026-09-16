package com.example.erp.attendance.interfaces.web;

import com.example.erp.attendance.api.AttendanceApi;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.web.dto.AttendanceWebResponse;
import com.example.erp.attendance.interfaces.web.dto.ClockInWebRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceWebController {

    private final AttendanceApi attendanceApi;

    public AttendanceWebController(AttendanceApi attendanceApi) {
        this.attendanceApi = attendanceApi;
    }

    @GetMapping("/{id}")
    public AttendanceWebResponse get(@PathVariable String id) {
        return toResponse(attendanceApi.findById(id));
    }

    @GetMapping
    public List<AttendanceWebResponse> byStudent(@RequestParam String studentId) {
        return attendanceApi.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/clock-in")
    public AttendanceWebResponse clockIn(@RequestBody ClockInWebRequest request) {
        return toResponse(attendanceApi.clockIn(new ClockInCommand(request.studentId(), request.clockInTime(), request.faceToken())));
    }

    private AttendanceWebResponse toResponse(AttendanceRecordDto dto) {
        return new AttendanceWebResponse(dto.id(), dto.studentId(), dto.status(), dto.clockInTime());
    }
}
