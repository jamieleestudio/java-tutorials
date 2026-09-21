package com.third.li;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 声明式 AI 服务接口：方法即调用，注解即提示词。
 */
public interface Assistant {

    @SystemMessage("你是一个精通 Java 与大模型应用开发的架构师助手，回答简洁专业。")
    @UserMessage("{{message}}")
    String chat(@V("message") String message);

    @SystemMessage("你是翻译专家。把用户内容翻译成英文，只输出译文。")
    @UserMessage("翻译：{{text}}")
    String translate(@V("text") String text);
}
