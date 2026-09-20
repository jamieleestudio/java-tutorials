package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 路由模式（Anthropic Routing 的 SAA Graph 实现）。
 *
 * <p>与 {@code ai/agentscope/agentscope-patterns/agentscope-routing}、
 * {@code ai/spring-ai/spring-ai-patterns/spring-ai-routing} 1:1 对照：
 * 输入先做意图分类，再分发给"配置各异的专家"（不同 systemPrompt / temperature）。
 * 在 SAA 里这张路由图可以用 Mermaid 可视化（GET /patterns/routing/diagram）。
 */
@Service
public class PatternRoutingService {

    private final CompiledGraph compiledGraph;

    public PatternRoutingService(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        StateGraph graph = new StateGraph("support_router", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "lane", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                .addNode("router", node_async(state -> {
                    String lane = chatClient.prompt()
                            .user("客户问题分类：billing（账单/扣费/退款）/ technical（故障/报错/使用）/ general（其他）。"
                                    + "只输出类名。\n问题：" + state.value("input", ""))
                            .call()
                            .content();
                    String normalized = lane == null ? "general" : lane.trim().toLowerCase();
                    if (!normalized.startsWith("billing") && !normalized.startsWith("technical")) {
                        normalized = "general";
                    }
                    return Map.of("lane", normalized);
                }))
                .addNode("billing_expert", node_async(state -> Map.of("answer",
                        chatClient.prompt()
                                .system("你是账单专家，谨慎对待退款承诺，先核实信息。")
                                .user(state.value("input", ""))
                                .call()
                                .content())))
                .addNode("technical_expert", node_async(state -> Map.of("answer",
                        chatClient.prompt()
                                .system("你是技术支持专家，给出可操作的排查步骤。")
                                .user(state.value("input", ""))
                                .call()
                                .content())))
                .addNode("general_expert", node_async(state -> Map.of("answer",
                        chatClient.prompt()
                                .system("你是通用客服，礼貌简洁。")
                                .user(state.value("input", ""))
                                .call()
                                .content())))
                .addEdge(StateGraph.START, "router")
                .addConditionalEdges("router",
                        AsyncEdgeAction.edge_async(state -> state.value("lane", "general")),
                        Map.of("billing", "billing_expert",
                                "technical", "technical_expert",
                                "general", "general_expert"))
                .addEdge("billing_expert", StateGraph.END)
                .addEdge("technical_expert", StateGraph.END)
                .addEdge("general_expert", StateGraph.END);

        this.compiledGraph = graph.compile();
    }

    public Map<String, Object> route(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
