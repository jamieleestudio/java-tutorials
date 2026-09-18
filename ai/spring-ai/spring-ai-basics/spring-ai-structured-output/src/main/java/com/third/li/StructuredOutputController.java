package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结构化输出（ChatClient.call().entity() + BeanOutputConverter）。
 *
 * <p>Spring AI 2.0 让模型直接返回强类型对象：
 * <ul>
 *   <li>{@code call().entity(MyClass.class)} — 用 {@code BeanOutputConverter} 自动生成
 *       JSON Schema 约束模型输出，再反序列化成对象</li>
 *   <li>配合 {@code StructuredOutputValidationAdvisor} 可以校验输出合法性并自动重试</li>
 * </ul>
 *
 * <p>与 Embabel 的 @AchievesGoal + AgentScope 的 call(Class<?>) 对照：
 * Spring AI 用 entity(Class) 实现同等"模型 → 强类型对象"能力。
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
    public java.util.List<MovieInfo> structuredList(
            @RequestParam(value = "message", defaultValue = "列出三部科幻电影") String message) {
        return chatClient.prompt(message)
                .call()
                .entity(new org.springframework.core.ParameterizedTypeReference<java.util.List<MovieInfo>>() {});
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
