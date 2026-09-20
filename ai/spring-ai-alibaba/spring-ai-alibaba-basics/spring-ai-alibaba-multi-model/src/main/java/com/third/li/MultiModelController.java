package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 多模道路由：按 provider 参数把请求分发给不同的 ChatModel。
 *
 * <p>多 ChatModel 场景下不再依赖自动配置的唯一 ChatClient.Builder，
 * 而是用 {@code ChatClient.create(chatModel)} 为每个模型显式构建客户端，
 * 由业务层（这里是 Controller）决定路由 —— 这也是编排模式里"路由"模式的基础。
 */
@RestController
public class MultiModelController {

    private final ChatModel deepseek;
    private final ChatModel qwen;
    private final boolean qwenReady;

    public MultiModelController(
            @Qualifier("openAiChatModel") ChatModel deepseek,
            @Value("${dashscope.api-key:}") String dashscopeApiKey) {
        this.deepseek = deepseek;
        this.qwenReady = dashscopeApiKey != null && !dashscopeApiKey.isBlank();
        this.qwen = MultiModelConfig.buildQwenModel(dashscopeApiKey);
    }

    /** 按 provider 路由：/model/ask?provider=deepseek|qwen。 */
    @GetMapping("/model/ask")
    public String ask(
            @RequestParam(defaultValue = "deepseek") String provider,
            @RequestParam(value = "message", defaultValue = "用一句话介绍你自己") String message) {
        if ("qwen".equalsIgnoreCase(provider)) {
            if (!qwenReady) {
                return "通义千问未就绪：请设置环境变量 DASHSCOPE_API_KEY 后重试。";
            }
            return ChatClient.create(qwen).prompt(message).call().content();
        }
        return ChatClient.create(deepseek).prompt(message).call().content();
    }

    /** 查看已装配的模型清单与就绪状态。 */
    @GetMapping("/model/providers")
    public Map<String, Object> providers() {
        return Map.of(
                "deepseek", Map.of("base-url", "https://api.deepseek.com",
                        "model", "deepseek-chat", "ready", true),
                "qwen", Map.of("base-url", MultiModelConfig.DASHSCOPE_BASE_URL,
                        "model", "qwen-plus", "ready", qwenReady));
    }
}
