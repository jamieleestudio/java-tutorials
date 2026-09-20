package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 结构化输出（ChatClient.call().entity() + BeanOutputConverter）。
 *
 * <p>{@code call().entity(Class)} 用 BeanOutputConverter 自动生成 JSON Schema
 * 约束模型输出，再反序列化成强类型对象。结构化输出是路由 / 编排模式的基础，
 * 在 Graph 编排（{@code spring-ai-alibaba-graph-conditional}）里大量使用。
 */
@RestController
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** entity(Class) 结构化输出：解析电影信息。 */
    @GetMapping("/ai/structured")
    public MovieInfo structured(
            @RequestParam(value = "message", defaultValue = "介绍电影《肖申克的救赎》") String message) {
        return chatClient.prompt(message)
                .call()
                .entity(MovieInfo.class);
    }

    /** entity(ParameterizedTypeReference) 结构化输出：解析列表。 */
    @GetMapping("/ai/structured/list")
    public List<MovieInfo> structuredList(
            @RequestParam(value = "message", defaultValue = "列出三部科幻电影") String message) {
        return chatClient.prompt(message)
                .call()
                .entity(new ParameterizedTypeReference<List<MovieInfo>>() {});
    }

    /** 电影信息 POJO。 */
    public record MovieInfo(
            String title,
            Integer year,
            String director,
            String genre,
            Double rating) {
    }
}
