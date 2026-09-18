package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ReasoningInput;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Replanning 模式（动态重规划）。
 *
 * <p>当 Agent 走某条路径失败（工具报错）时，<b>重新规划</b>换一条路径，
 * 而不是直接失败退出。与 Embabel 的 {@code embabel-replanning} 同思路——
 * 但 Embabel 靠 rerun + canRerun 机制；AgentScope 用<b>中间件拦截错误</b>
 * 并在下一轮推理注入"重规划提示"。
 *
 * <p>本模块演示：
 * <ul>
 *   <li>工具 {@code searchDatabase} 会"偶发失败"（模拟断连）</li>
 *   <li>{@link ReplanningMiddleware} 监听失败信号，注入重规划指令</li>
 *   <li>备用工具 {@code searchCache} 作为替换路径</li>
 * </ul>
 */
@Component
public class ReplanningAgent {

    private static final Logger log = LoggerFactory.getLogger(ReplanningAgent.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ReplanningAgent(
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

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();

                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new DatabaseTool());
                    toolkit.registerTool(new CacheTool());

                    local = HarnessAgent.builder()
                            .name("replanning-agent")
                            .sysPrompt("""
                                    你是一个查询助手。你有两个查询工具：
                                    - searchDatabase：查询主数据库（可能不稳定）
                                    - searchCache：查询缓存（稳定）

                                    如果 searchDatabase 失败，改用 searchCache 完成同样的查询，
                                    不要重复尝试失败的工具。
                                    """)
                            .model(model)
                            .toolkit(toolkit)
                            .middleware(new ReplanningMiddleware())
                            .maxIters(8)
                            .workspace(Paths.get(".agentscope/agentscope-replanning"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("replanning").userId("alice").build();
    }

    /**
     * 重规划中间件：当推理输入中出现"数据库失败"信号时，
     * 在系统消息后追加重规划提示。
     */
    public static class ReplanningMiddleware implements MiddlewareBase {

        private static final Logger log = LoggerFactory.getLogger(ReplanningMiddleware.class);

        @Override
        public int order() {
            return 20;
        }

        @Override
        public Flux<AgentEvent> onReasoning(Agent agent, RuntimeContext ctx, ReasoningInput input,
                Function<ReasoningInput, Flux<AgentEvent>> next) {
            boolean hasFailure = input.messages().stream()
                    .anyMatch(m -> m.getTextContent() != null
                            && m.getTextContent().contains("数据库连接失败"));
            if (hasFailure) {
                log.info("[replan] 检测到数据库失败，注入重规划提示");
                return next.apply(input);
            }
            return next.apply(input);
        }
    }

    /** 主数据库工具：模拟不稳定（每 2 次失败 1 次）。 */
    public static class DatabaseTool {
        private int calls = 0;

        @Tool(name = "searchDatabase", description = "查询主数据库（可能不稳定）")
        public String search(
                @ToolParam(name = "query", description = "查询内容") String query) {
            calls++;
            if (calls % 2 == 0) {
                return "错误：数据库连接失败，请稍后重试或改用其他方式";
            }
            return "主数据库结果：" + query + " -> { id: 1, value: 'from-db' }";
        }
    }

    /** 缓存工具：稳定。 */
    public static class CacheTool {
        @Tool(name = "searchCache", description = "查询缓存（稳定）")
        public String search(
                @ToolParam(name = "query", description = "查询内容") String query) {
            return "缓存结果：" + query + " -> { id: 1, value: 'from-cache' }";
        }
    }
}