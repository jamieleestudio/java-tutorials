package com.third.li;

import io.agentscope.core.permission.PermissionMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 权限模式接口。 */
@RestController
public class PermissionModeAgentController {

    private final PermissionModeAgent agent;

    public PermissionModeAgentController(PermissionModeAgent agent) {
        this.agent = agent;
    }

    /** DEFAULT 模式：危险操作需确认。 */
    @GetMapping("/permission/default")
    public String defaultMode(
            @RequestParam(value = "message", defaultValue = "帮我删除 /tmp/test.txt") String message) {
        return agent.chat(message, PermissionMode.DEFAULT);
    }

    /** ACCEPT_EDITS 模式：自动接受编辑。 */
    @GetMapping("/permission/accept-edits")
    public String acceptEdits(
            @RequestParam(value = "message", defaultValue = "帮我创建 /tmp/test.txt") String message) {
        return agent.chat(message, PermissionMode.ACCEPT_EDITS);
    }

    /** EXPLORE 模式：只读。 */
    @GetMapping("/permission/explore")
    public String explore(
            @RequestParam(value = "message", defaultValue = "查看 /tmp 目录") String message) {
        return agent.chat(message, PermissionMode.EXPLORE);
    }

    /** BYPASS 模式：跳过权限检查。 */
    @GetMapping("/permission/bypass")
    public String bypass(
            @RequestParam(value = "message", defaultValue = "执行 rm -rf /tmp/test") String message) {
        return agent.chat(message, PermissionMode.BYPASS);
    }

    /** DONT_ASK 模式：不询问，按规则自动决策。 */
    @GetMapping("/permission/dont-ask")
    public String dontAsk(
            @RequestParam(value = "message", defaultValue = "执行 ls -la") String message) {
        return agent.chat(message, PermissionMode.DONT_ASK);
    }

    /** 查看当前模式。 */
    @GetMapping("/permission/current")
    public String current() {
        return "当前权限模式：" + agent.currentMode();
    }
}