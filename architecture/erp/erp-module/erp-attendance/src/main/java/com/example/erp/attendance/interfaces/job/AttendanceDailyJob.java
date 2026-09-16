package com.example.erp.attendance.interfaces.job;

import com.example.erp.attendance.api.AttendanceApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceDailyJob {

    private final AttendanceApi attendanceApi;

    public AttendanceDailyJob(AttendanceApi attendanceApi) {
        this.attendanceApi = attendanceApi;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        attendanceApi.findByStudentId("daily-check");
    }
}