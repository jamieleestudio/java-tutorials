package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Graph 编排基础接口。
 *
 * <p>GET /graph/run — 执行整张图，返回最终状态（含 input/keywords/article）。
 * GET /graph/mermaid — 输出图的 Mermaid 定义，可视化图结构。
 */
@RestController
public class GraphBasicsController {

    private final GraphBasicsService graphService;

    public GraphBasicsController(GraphBasicsService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph/run")
    public Map<String, Object> run(
            @RequestParam(value = "message",
                    defaultValue = "Spring AI Alibaba 让 Java 开发者可以用图的方式编排大模型工作流")
            String message) throws Exception {
        return graphService.run(message);
    }

    @GetMapping(value = "/graph/mermaid", produces = "text/plain;charset=UTF-8")
    public String mermaid() {
        return graphService.mermaid();
    }
}
