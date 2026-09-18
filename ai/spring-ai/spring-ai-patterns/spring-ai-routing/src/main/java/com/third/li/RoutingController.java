package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Routing 模式（路由分类）。
 *
 * <p>先用 entity(Category.class) 结构化输出分类标签，再 switch 到不同
 * system prompt 的专用 Agent 处理。与 Embabel @Condition / AgentScope 的
 * structured-output 路由对照。
 */
@RestController
public class RoutingController {

    private final ChatClient classifier;
    private final ChatClient techClient;
    private final ChatClient lifeClient;
    private final ChatClient defaultClient;

    public RoutingController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.classifier = ChatClient.create(chatModel);
        this.techClient = chatClientBuilder
                .defaultSystem("你是技术专家，用严谨技术视角回答，给出原理和示例。")
                .build();
        this.lifeClient = chatClientBuilder
                .defaultSystem("你是生活助手，用轻松实用的口吻回答。")
                .build();
        this.defaultClient = ChatClient.create(chatModel);
    }

    /** 分类并路由到对应角色的 Agent。 */
    @GetMapping("/ai/routing")
    public String route(
            @RequestParam(value = "message", defaultValue = "如何优化 Java 应用内存") String message) {
        // 1. 结构化输出分类（模型自主判断）
        Category category = classifier.prompt(message).call().entity(Category.class);
        String type = category != null && category.type() != null ? category.type() : "其它";

        // 2. 路由到对应 ChatClient
        return switch (type) {
            case "技术" -> "【技术 Agent】\n" + techClient.prompt(message).call().content();
            case "生活" -> "【生活 Agent】\n" + lifeClient.prompt(message).call().content();
            default -> "【默认 Agent】\n" + defaultClient.prompt(message).call().content();
        };
    }

    /** 分类结果：模型输出 { "type": "技术" }。 */
    public record Category(String type) {
    }
}
