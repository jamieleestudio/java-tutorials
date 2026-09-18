package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 多目标选择接口。 */
@RestController
public class MultiGoalAgentController {

    private final MultiGoalAgent agent;

    public MultiGoalAgentController(MultiGoalAgent agent) {
        this.agent = agent;
    }

    /** 定义型输出。 */
    @GetMapping("/patterns/multi-goal/definition")
    public String definition(
            @RequestParam(value = "message", defaultValue = "什么是微服务架构") String message) {
        return agent.askDefinition(message);
    }

    /** 步骤型输出。 */
    @GetMapping("/patterns/multi-goal/recipe")
    public String recipe(
            @RequestParam(value = "message", defaultValue = "如何搭建一个 Spring Boot 项目") String message) {
        return agent.askRecipe(message);
    }

    /** 对比型输出。 */
    @GetMapping("/patterns/multi-goal/comparison")
    public String comparison(
            @RequestParam(value = "message", defaultValue = "单体架构和微服务架构的对比") String message) {
        return agent.askComparison(message);
    }
}