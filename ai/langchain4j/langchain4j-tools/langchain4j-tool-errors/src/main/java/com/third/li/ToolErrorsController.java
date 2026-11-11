package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 工具错误处理接口：GET /tools/errors/… */
@RestController
@RequestMapping("/tools/errors")
public class ToolErrorsController {

    private final ToolErrorsConfig.OpsAssistant opsAssistant;

    public ToolErrorsController(ToolErrorsConfig.OpsAssistant opsAssistant) {
        this.opsAssistant = opsAssistant;
    }

    /** LlmVisibleToolExecutionException：错误说明对 LLM 可见，模型会向用户解释并给建议。 */
    @GetMapping("/visible")
    public String visible(@RequestParam(defaultValue = "帮我查一下 SKU-9901 的实时库存") String message) {
        return opsAssistant.chat(message);
    }

    /** 普通 RuntimeException：内部细节默认不进 LLM 上下文，模型只会看到通用失败。 */
    @GetMapping("/hidden")
    public String hidden(@RequestParam(defaultValue = "把 login 操作写进审计日志") String message) {
        return opsAssistant.chat(message);
    }
}
