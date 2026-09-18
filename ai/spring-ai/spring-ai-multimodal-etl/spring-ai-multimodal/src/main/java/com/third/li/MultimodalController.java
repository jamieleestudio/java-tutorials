package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多模态（图片输入 + Media）。
 *
 * <p>Spring AI 2.0 用 {@link Media} 携带图片/音频等非文本内容：
 * <ul>
 *   <li>{@code UserMessage.builder().media(mimeType, resource)} — 消息级图片</li>
 *   <li>{@code ChatClient.prompt().user(u -> u.media(mimeType, resource))} — 请求级图片</li>
 * </ul>
 *
 * <p>需要模型支持视觉输入（如 gpt-4o / deepseek-vl）。
 * 本模块默认用 resources 里的示例图片。
 */
@RestController
public class MultimodalController {

    private final ChatClient chatClient;

    public MultimodalController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** 附带图片提问（图片来自 classpath:static/）。 */
    @GetMapping("/ai/multimodal")
    public String multimodal(
            @RequestParam(value = "message", defaultValue = "这张图片里有什么？请描述一下") String message) {
        var resource = new ClassPathResource("static/sample.jpg");
        if (!resource.exists()) {
            return "未找到示例图片 static/sample.jpg。请放入图片后重试。";
        }
        return chatClient.prompt(message)
                .user(u -> u
                        .text(message)
                        .media(new Media(MediaType.IMAGE_JPEG, resource)))
                .call()
                .content();
    }
}
