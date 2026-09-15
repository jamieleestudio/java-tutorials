package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import io.a2a.spec.AgentCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A2A 演示接口。
 *
 * <p>本应用同时是 **A2A 服务端**（由 starter-a2a 自动暴露）与**客户端**：
 * <ul>
 *   <li>服务端：{@code GET /a2a/.well-known/agent.json}（Agent Card）、{@code POST /a2a}（JSON-RPC）</li>
 *   <li>{@code GET /a2a/card}：以客户端身份拉取远端 Agent Card</li>
 *   <li>{@code GET /a2a/ask}：直接以 A2A 客户端调用远端智能体</li>
 *   <li>{@code GET /a2a/delegate}：本地 Agent 把远端智能体当工具用（Agent-to-Agent）</li>
 * </ul>
 */
@RestController
public class A2aController {

    private final AgentPlatform agentPlatform;
    private final A2AClient client;

    public A2aController(AgentPlatform agentPlatform, A2AClient client) {
        this.agentPlatform = agentPlatform;
        this.client = client;
    }

    /** 拉取远端 Agent Card（技能列表 / 传输方式） */
    @GetMapping("/a2a/card")
    public AgentCard card() throws Exception {
        return client.agentCard();
    }

    /** 直接调用远端 A2A 智能体 */
    @GetMapping("/a2a/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message", defaultValue = "用一句话介绍 Embabel") String message) throws Exception {
        return Map.of("message", message, "remoteReply", client.send(message));
    }

    /** 本地 Agent 把远端智能体当工具调用 */
    @GetMapping("/a2a/delegate")
    public DelegatedReply delegate(
            @RequestParam(value = "message", defaultValue = "请远端智能体用一句话说明什么是 A2A 协议") String message) {
        return AgentInvocation.create(agentPlatform, DelegatedReply.class).invoke(new UserInput(message));
    }
}
