package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 提示模板（PromptTemplate + 参数化）。
 *
 * <p>Spring AI 用 {@link PromptTemplate} 做提示词复用：
 * <ul>
 *   <li>模板串里用 {@code {var}} 占位符（ST4 语法）</li>
 *   <li>{@code render(Map)} 渲染变量</li>
 *   <li>{@code createMessage(Map)} 直接生成用户消息</li>
 * </ul>
 *
 * <p>也可以用 ChatClient 的 {@code user(Consumer<PromptUserSpec>)} 配合
 * {@code .param()/.params()} 参数化。
 */
@RestController
public class PromptTemplatesController {

    private final ChatClient chatClient;

    public PromptTemplatesController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** PromptTemplate + render：预渲染模板再调用。 */
    @GetMapping("/ai/prompt")
    public String promptTemplate(
            @RequestParam(value = "topic", defaultValue = "Java 虚拟线程") String topic,
            @RequestParam(value = "audience", defaultValue = "初学者") String audience) {
        String template = "请用适合{audience}的方式，用 {maxWords} 字以内介绍 {topic} 的核心概念。";
        String rendered = new PromptTemplate(template)
                .render(Map.of("audience", audience, "topic", topic, "maxWords", 100));
        return chatClient.prompt(rendered).call().content();
    }

    /** ChatClient 参数化：直接用 param() 注入变量。 */
    @GetMapping("/ai/prompt/param")
    public String param(
            @RequestParam(value = "topic", defaultValue = "Spring AI") String topic,
            @RequestParam(value = "style", defaultValue = "幽默") String style) {
        return chatClient.prompt()
                .user(u -> u.text("请用{style}风格介绍{topic}。").param("style", style).param("topic", topic))
                .call()
                .content();
    }
}
