package com.third.li;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Boot starter 集成。
 *
 * <p>本模块同时引入 langchain4j-spring-boot-starter 与
 * langchain4j-open-ai-spring-boot-starter（均基于 Boot 3.5.13 构建）。
 * 若 starter 的自动装配在 Boot 4 环境下不可用，本模块演示的
 * 手动 Bean 注册方式是完全等价的替代方案（官方文档确认）。
 */
@RestController
public class SpringBootController {

    private final dev.langchain4j.model.chat.ChatModel chatModel;

    public SpringBootController(dev.langchain4j.model.chat.ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/spring")
    public String chat(@RequestParam(defaultValue = "用一句话说明 LangChain4j 与 Spring Boot 的集成方式") String message) {
        return chatModel.chat(message);
    }
}
