package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时调度接口。
 *
 * GET /agent/schedule/status — 查看任务与执行记录。
 * POST /agent/schedule/cancel — 注销任务。
 */
@RestController
public class AgentSchedulingController {

    private final AgentSchedulingService service;

    public AgentSchedulingController(AgentSchedulingService service) {
        this.service = service;
    }

    @GetMapping("/agent/schedule/status")
    public String status() {
        return service.status();
    }

    @PostMapping("/agent/schedule/cancel")
    public String cancel() {
        return service.cancel();
    }
}
