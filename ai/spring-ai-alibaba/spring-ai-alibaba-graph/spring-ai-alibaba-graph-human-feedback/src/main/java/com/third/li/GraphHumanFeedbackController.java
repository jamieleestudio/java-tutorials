package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 人机协同接口。
 *
 * <p>POST /graph/hitl/start — 启动流程，执行到人工节点挂起。
 * POST /graph/hitl/resume — 提交人工反馈，从断点恢复执行。
 */
@RestController
public class GraphHumanFeedbackController {

    private final GraphHumanFeedbackService graphService;

    public GraphHumanFeedbackController(GraphHumanFeedbackService graphService) {
        this.graphService = graphService;
    }

    @PostMapping("/graph/hitl/start")
    public Map<String, Object> start(
            @RequestParam(value = "message", defaultValue = "策划一次 30 分钟的 Spring AI Alibaba 技术分享")
            String message) throws Exception {
        return graphService.start(message);
    }

    @PostMapping("/graph/hitl/resume")
    public Map<String, Object> resume(
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "feedback", defaultValue = "第二条改成现场演示，时间控制在 5 分钟内")
            String feedback) throws Exception {
        return graphService.resume(threadId, feedback);
    }
}
