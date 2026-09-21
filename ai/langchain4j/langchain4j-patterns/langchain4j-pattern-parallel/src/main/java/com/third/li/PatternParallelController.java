package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 并行模式接口：GET /patterns/parallel?mood=… */
@RestController
public class PatternParallelController {

    private final PatternParallelConfig.EveningPlannerAgent planner;

    public PatternParallelController(PatternParallelConfig.EveningPlannerAgent planner) {
        this.planner = planner;
    }

    @GetMapping("/patterns/parallel")
    public List<String> plan(@RequestParam(defaultValue = "浪漫") String mood) {
        return planner.plan(mood);
    }
}
