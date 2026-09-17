package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.subagent.RemoteSubagentStub;
import io.agentscope.harness.agent.subagent.task.RemoteTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Agent-to-Agent（A2A）远程通信。
 *
 * <p>AgentScope 的 A2A 协议支持<b>远程 Agent</b>之间的通信：
 * <ul>
 *   <li>{@link RemoteTarget} — 远程 Agent 地址</li>
 *   <li>{@link RemoteSubagentStub} — 远程子 Agent 桩</li>
 *   <li>{@code AgentProtocolTransport} — 传输层（HTTP/gRPC）</li>
 * </ul>
 *
 * <p>工作流：
 * <ol>
 *   <li>主 Agent 通过 {@code agentSpawn} 创建远程子 Agent</li>
 *   <li>远程 Agent 在另一个节点执行</li>
 *   <li>结果通过协议传输回主 Agent</li>
 * </ol>
 *
 * <p>适合分布式 Agent 集群、微服务化 Agent。
 */
@Component
public class A2aAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public A2aAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    public String describeA2A() {
        return """
                A2A (Agent-to-Agent) 通信：
                1. RemoteTarget — 远程 Agent 地址
                   new RemoteTarget("http://remote-host:9140", "agent-id")
                2. AgentProtocolTransport — 传输层
                   transport.submit(taskRunSpec) — 提交任务到远程
                3. RemoteSubagentStub — 远程子 Agent 桩
                   stub.call(msgs) — 调用远程 Agent

                架构：
                主 Agent → AgentProtocolTransport → 远程 Agent → 结果返回

                使用场景：
                - 分布式 Agent 集群
                - 不同语言 Agent 互调
                - Agent 微服务化
                """;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("a2a-agent")
                            .sysPrompt("你是一个支持 A2A 通信的助手。你可以委派任务给远程 Agent。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-a2a"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("a2a-demo").userId("alice").build();
    }
}