package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 条件边路由接口：GET /graph/route?message=…
 */
@RestController
public class GraphConditionalController {

    private final GraphConditionalService graphService;

    public GraphConditionalController(GraphConditionalService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph/route")
    public Map<String, Object> route(
            @RequestParam(value = "message", defaultValue = "Spring AI Alibaba 的 Graph 编排怎么用？")
            String message) throws Exception {
        return graphService.route(message);
    }
}
