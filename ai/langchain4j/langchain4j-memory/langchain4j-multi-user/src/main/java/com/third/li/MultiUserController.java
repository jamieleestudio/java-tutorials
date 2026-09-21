package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多用户接口：GET /ai/memory/user?userId=user-a&amp;message=…
 * 不同 userId 的记忆互不可见。
 */
@RestController
public class MultiUserController {

    private final UserAssistant assistant;

    public MultiUserController(UserAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/memory/user")
    public String chat(
            @RequestParam(defaultValue = "user-a") String userId,
            @RequestParam(defaultValue = "你好，我是在测试多用户记忆") String message) {
        return assistant.chat(userId, message);
    }
}
