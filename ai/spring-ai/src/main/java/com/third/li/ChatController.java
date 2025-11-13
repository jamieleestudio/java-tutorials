package com.third.li;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final DeepSeekChatModel chatModel;

    public ChatController(DeepSeekChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 402 Payment Required  需要支付费用
     * @param message 消息
     * @return Flux<String>
     */
    @GetMapping("/ai/generate")
    public Flux<String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return chatModel.stream(message);
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

}
