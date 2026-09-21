package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 并行工作流接口：GET /ai/agentic/parallel?mood=… */
@RestController
public class AgenticParallelController {

    private final AgenticParallelConfig.EveningPlannerAgent planner;

    public AgenticParallelController(AgenticParallelConfig.EveningPlannerAgent planner) {
        this.planner = planner;
    }

    @GetMapping("/ai/agentic/parallel")
    public List<String> plan(@RequestParam(defaultValue = "浪漫") String mood) {
        return planner.plan(mood);
    }
}
