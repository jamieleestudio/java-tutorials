package com.third.li;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.a2a.server.AgentScopeA2aServer;
import io.agentscope.core.a2a.server.card.ConfigurableAgentCard;
import io.agentscope.core.a2a.server.executor.runner.AgentRequestOptions;
import io.agentscope.core.a2a.server.executor.runner.AgentRunner;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * A2A（Agent-to-Agent）协议 Agent：用 {@link AgentScopeA2aServer} 把本地 Agent
 * 暴露成符合 A2A 协议的远程可调用 Agent。
 *
 * <p>A2A 协议让不同框架/平台的 Agent 能互相发现和调用。{@link AgentScopeA2aServer} 负责：
 * <ul>
 *   <li>发布 AgentCard（能力描述：name/description/skills）</li>
 *   <li>处理远程 task 请求（JSON-RPC over HTTP/SSE）</li>
 *   <li>把 A2A 请求转成本地 Agent 调用（通过 {@link AgentRunner}）</li>
 * </ul>
 *
 * <p>本例用 {@link AgentRunner} 适配本地 {@link HarnessAgent}，
 * {@code AgentScopeA2aServer.builder(runner).agentCard(card).build()} 创建服务。
 */
@Component
public class A2aAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-a2a";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile AgentScopeA2aServer a2aServer;

    public A2aAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    public String a2aStatus() {
        AgentScopeA2aServer server = a2aServer();
        return "A2A 服务已启动，AgentCard: " + server.getAgentCard().name();
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
                            .sysPrompt("你是一个可通过 A2A 协议被远程调用的助手。")
                            .model(model)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private AgentScopeA2aServer a2aServer() {
        AgentScopeA2aServer local = a2aServer;
        if (local == null) {
            synchronized (this) {
                local = a2aServer;
                if (local == null) {
                    HarnessAgent harness = agent();
                    AgentRunner runner = new HarnessAgentRunner(harness, "a2a-agent",
                            "可通过 A2A 协议远程调用的助手");
                    ConfigurableAgentCard card = new ConfigurableAgentCard.Builder()
                            .name("a2a-agent")
                            .description("AgentScope A2A demo agent")
                            .build();
                    local = AgentScopeA2aServer.builder(runner)
                            .agentCard(card)
                            .build();
                    a2aServer = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("a2a-demo").userId("alice").build();
    }

    /** 把 {@link HarnessAgent} 适配成 A2A 服务需要的 {@link AgentRunner}。 */
    static class HarnessAgentRunner implements AgentRunner {
        private final HarnessAgent agent;
        private final String name;
        private final String description;

        HarnessAgentRunner(HarnessAgent agent, String name, String description) {
            this.agent = agent;
            this.name = name;
            this.description = description;
        }

        @Override
        public String getAgentName() {
            return name;
        }

        @Override
        public String getAgentDescription() {
            return description;
        }

        @Override
        public reactor.core.publisher.Flux<AgentEvent> streamEvents(
                List<Msg> msgs, AgentRequestOptions options) {
            String sid = options != null && options.getTaskId() != null ? options.getTaskId() : "a2a";
            String uid = options != null && options.getUserId() != null ? options.getUserId() : "remote";
            RuntimeContext ctx = RuntimeContext.builder()
                    .sessionId(sid).userId(uid).build();
            io.agentscope.core.message.Msg reply = agent.call(msgs, ctx).block();
            io.agentscope.core.event.AgentResultEvent resultEvent =
                    new io.agentscope.core.event.AgentResultEvent(reply);
            return reactor.core.publisher.Flux.just(resultEvent);
        }

        @Override
        public void stop(String requestId) {
            agent.interrupt();
        }
    }
}