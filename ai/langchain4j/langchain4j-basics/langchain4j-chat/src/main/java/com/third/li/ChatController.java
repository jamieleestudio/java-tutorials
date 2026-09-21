package com.third.li;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ChatModel 低层 API：chat(String) 便捷重载与 chat(ChatRequest) 完整形式。
 *
 * <p>对照 Java 侧其他组：Spring AI 的 ChatModel、SAA 的 ChatModel、
 * LangChain4j 的 ChatModel —— 各框架对"模型调用"这一低层抽象的命名趋同。
 */
@RestController
public class ChatController {

    private final ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** 便捷重载：直接传用户消息，返回纯文本。 */
    @GetMapping("/ai/chat")
    public String chat(@RequestParam(defaultValue = "用一句话介绍 LangChain4j") String message) {
        return chatModel.chat(message);
    }

    /** 完整形式：ChatRequest + ChatResponse（含 token 用量与 finishReason）。 */
    @GetMapping("/ai/chat/response")
    public ChatResponse chatResponse(@RequestParam(defaultValue = "9.11 和 9.9 哪个大？") String message) {
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(message))
                .build();
        return chatModel.chat(request);
    }
}
