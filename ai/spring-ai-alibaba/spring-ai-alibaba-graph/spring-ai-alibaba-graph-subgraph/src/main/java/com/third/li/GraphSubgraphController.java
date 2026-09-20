package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 子图组合接口：GET /graph/subgraph/run
 */
@RestController
public class GraphSubgraphController {

    private final GraphSubgraphService graphService;

    public GraphSubgraphController(GraphSubgraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph/subgraph/run")
    public Map<String, Object> run(
            @RequestParam(value = "message", defaultValue = "这个框架把大模型工作流变成了一张可以画出来的图")
            String message) throws Exception {
        return graphService.run(message);
    }
}
