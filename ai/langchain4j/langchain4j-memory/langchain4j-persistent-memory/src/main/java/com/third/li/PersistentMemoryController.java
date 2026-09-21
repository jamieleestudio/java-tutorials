package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 持久化记忆接口：write 落盘 / read 重启后仍可读。 */
@RestController
public class PersistentMemoryController {

    private final PersistentMemoryConfig.PersistentAssistant assistant;

    public PersistentMemoryController(PersistentMemoryConfig.PersistentAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/memory/write")
    public String write(@RequestParam(defaultValue = "记住：团队代号是 NightOwl") String message) {
        return assistant.chat(message);
    }

    @GetMapping("/ai/memory/read")
    public String read(@RequestParam(defaultValue = "团队代号是什么？") String message) {
        return assistant.chat(message);
    }
}
