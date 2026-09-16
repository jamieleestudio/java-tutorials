package com.example.erp.attendance.interfaces.job;

import com.example.erp.attendance.api.AttendanceQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceDailyJob {

    private final AttendanceQueryService queryService;

    public AttendanceDailyJob(AttendanceQueryService queryService) {
        this.queryService = queryService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        queryService.findByStudentId("daily-check");
    }
}