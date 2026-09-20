package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件工具接口：GET /agent/fs/ask、GET /agent/fs/workspace
 */
@RestController
public class AgentFilesystemController {

    private final AgentFilesystemService service;

    public AgentFilesystemController(AgentFilesystemService service) {
        this.service = service;
    }

    @GetMapping("/agent/fs/ask")
    public String ask(
            @RequestParam(value = "message",
                    defaultValue = "在 notes.md 末尾追加一条：2. Graph 的 checkpoint 可以落盘恢复，然后把 todo.txt 重命名为 plan.txt")
            String message) throws Exception {
        return service.ask(message);
    }

    @GetMapping("/agent/fs/workspace")
    public String workspace() throws Exception {
        return service.workspaceSnapshot();
    }
}
