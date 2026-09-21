package com.third.li;

import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agentic 基础接口。
 *
 * GET /ai/agentic/story?topic=…&style=… — 顺序工作流：写故事 → 改风格。
 * GET /ai/agentic/scope — 说明 AgenticScope 共享状态与持久化 SPI。
 */
@RestController
public class AgenticBasicsController {

    private final UntypedAgent storyWorkflow;

    public AgenticBasicsController(UntypedAgent storyWorkflow) {
        this.storyWorkflow = storyWorkflow;
    }

    @GetMapping("/ai/agentic/story")
    public String story(
            @RequestParam(defaultValue = "龙与法师的冒险") String topic,
            @RequestParam(defaultValue = "幽默") String style) {
        return (String) storyWorkflow.invoke(java.util.Map.of(
                "topic", topic,
                "style", style));
    }

    @GetMapping("/ai/agentic/scope")
    public String scope() {
        return """
                AgenticScope 是 langchain4j-agentic 的共享状态容器：
                  - outputKey 把 Agent 输出写入作用域
                  - @V 参数从作用域读取
                  - AgenticScopePersister SPI 可把作用域持久化到外部存储
                本组 agentic-observability 模块演示 AgentMonitor 的执行树报告。
                """;
    }
}
