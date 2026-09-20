package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 工具级 HITL 接口。
 *
 * GET /agent/tool-hitl/start — 起草邮件，执行到工具节点前挂起。
 * POST /agent/tool-hitl/approve — 人工审批（approved=true 发送 / false 驳回）。
 */
@RestController
public class GraphToolHitlController {

    private final GraphToolHitlService service;

    public GraphToolHitlController(GraphToolHitlService service) {
        this.service = service;
    }

    @GetMapping("/agent/tool-hitl/start")
    public Map<String, Object> start(
            @RequestParam(value = "message",
                    defaultValue = "通知全体会员本周六上午十点进行系统维护，预计持续两小时")
            String message) throws Exception {
        return service.start(message);
    }

    @PostMapping("/agent/tool-hitl/approve")
    public Map<String, Object> approve(
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "approved", defaultValue = "true") boolean approved) throws Exception {
        return service.approve(threadId, approved);
    }
}
