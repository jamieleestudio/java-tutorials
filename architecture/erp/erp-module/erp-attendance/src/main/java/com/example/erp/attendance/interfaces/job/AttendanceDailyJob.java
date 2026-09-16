package com.example.erp.attendance.interfaces.job;

import com.example.erp.attendance.application.AttendanceApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceDailyJob {

    private final AttendanceApplicationService attendanceApplicationService;

    public AttendanceDailyJob(AttendanceApplicationService attendanceApplicationService) {
        this.attendanceApplicationService = attendanceApplicationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        attendanceApplicationService.findByStudentId("daily-check");
    }
}