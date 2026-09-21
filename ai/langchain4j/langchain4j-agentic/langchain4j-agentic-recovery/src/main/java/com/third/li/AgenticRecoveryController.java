package com.third.li;

import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 错误恢复接口。
 *
 * GET /ai/agentic/recovery/retry — 缺参场景：errorHandler 补默认值后重试成功。
 * GET /ai/agentic/recovery/normal — 正常入参直通。
 */
@RestController
public class AgenticRecoveryController {

    private final UntypedAgent recoveryWorkflow;

    public AgenticRecoveryController(UntypedAgent recoveryWorkflow) {
        this.recoveryWorkflow = recoveryWorkflow;
    }

    @GetMapping("/ai/agentic/recovery/retry")
    public String retry() {
        // 故意缺失 topic，触发 errorHandler 补值重试
        return (String) recoveryWorkflow.invoke(java.util.Map.of("audience", "大学生"));
    }

    @GetMapping("/ai/agentic/recovery/normal")
    public String normal(
            @RequestParam(defaultValue = "龙与法师") String topic,
            @RequestParam(defaultValue = "大学生") String audience) {
        return (String) recoveryWorkflow.invoke(java.util.Map.of(
                "topic", topic,
                "audience", audience));
    }
}
