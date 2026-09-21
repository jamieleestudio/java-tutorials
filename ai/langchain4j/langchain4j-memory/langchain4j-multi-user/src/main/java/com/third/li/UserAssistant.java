package com.third.li;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/** 多用户 AI 服务：方法第一个参数 @MemoryId 声明会话归属。 */
public interface UserAssistant {

    @UserMessage("{{message}}")
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
