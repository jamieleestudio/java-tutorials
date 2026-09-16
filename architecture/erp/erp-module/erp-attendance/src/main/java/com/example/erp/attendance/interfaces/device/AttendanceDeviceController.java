package com.example.erp.attendance.interfaces.device;

import com.example.erp.attendance.api.AttendanceApi;
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

    private final AttendanceApi attendanceApi;

    public AttendanceDeviceController(AttendanceApi attendanceApi) {
        this.attendanceApi = attendanceApi;
    }

    @PostMapping("/clock")
    public AttendanceDeviceResponse clock(@RequestBody DeviceClockRequest request) {
        AttendanceRecordDto dto = attendanceApi.clockIn(new ClockInCommand(request.studentId(), request.clockInTime(), request.faceToken()));
        return new AttendanceDeviceResponse(dto.id(), dto.status());
    }
}