package com.example.erp.attendance.interfaces.device;

import com.example.erp.attendance.application.AttendanceApplicationService;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.interfaces.device.dto.AttendanceDeviceResponse;
import com.example.erp.attendance.interfaces.device.dto.DeviceClockRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/v1/device/attendance")
public class AttendanceDeviceController {

    private final AttendanceApplicationService attendanceApplicationService;

    public AttendanceDeviceController(AttendanceApplicationService attendanceApplicationService) {
        this.attendanceApplicationService = attendanceApplicationService;
    }

    @PostMapping("/clock")
    public AttendanceDeviceResponse clock(@RequestBody DeviceClockRequest request) {
        AttendanceRecordDto dto = attendanceApplicationService.clockIn(new ClockInCommand(request.studentId(), request.clockInTime(), request.faceToken()));
        return new AttendanceDeviceResponse(dto.id(), dto.status());
    }
}