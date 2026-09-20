package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 条件边路由：addConditionalEdges + 分类节点 + 多分支。
 *
 * <p>图的分支控制由两部分组成：
 * <ul>
 *   <li>路由动作 — {@code edge_async(EdgeAction)}：读状态返回一个"分支名"字符串；
 *       这里用结构化输出让模型先分类（tech / food / other）</li>
 *   <li>条件边 — {@code addConditionalEdges(source, router, Map.of(分支名 → 目标节点))}：
 *       按路由动作的返回值选择下一个节点，等价于 LangGraph 的 conditional_edges</li>
 * </ul>
 *
 * <p>与 {@code ai/spring-ai/spring-ai-patterns/spring-ai-routing}（手写 if/else 分发）对照：
 * Graph 把分支拓扑声明在图上，路由逻辑只负责"选边"。
 */
@Service
public class GraphConditionalService {

    private final CompiledGraph compiledGraph;

    public GraphConditionalService(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        StateGraph graph = new StateGraph("routing_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "category", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                // 分类节点：结构化输出决定走哪条分支
                .addNode("classify", node_async(state -> {
                    String input = state.value("input", "");
                    String category = chatClient.prompt()
                            .user("把问题分类为 tech / food / other 三类之一，只输出类名：\n" + input)
                            .call()
                            .content();
                    // 模型可能输出多余文字，做一次归一化
                    String normalized = category == null ? "other" : category.trim().toLowerCase();
                    if (!normalized.startsWith("tech") && !normalized.startsWith("food")) {
                        normalized = "other";
                    }
                    return Map.of("category", normalized);
                }))
                // 三个专家分支
                .addNode("tech_writer", node_async(state -> Map.of("answer",
                        chatClient.prompt().user("用技术视角简要回答：\n" + state.value("input", "")).call().content())))
                .addNode("food_writer", node_async(state -> Map.of("answer",
                        chatClient.prompt().user("用美食视角简要回答：\n" + state.value("input", "")).call().content())))
                .addNode("general_writer", node_async(state -> Map.of("answer",
                        chatClient.prompt().user("简洁回答：\n" + state.value("input", "")).call().content())))
                // 入边
                .addEdge(StateGraph.START, "classify")
                // 条件边：按 classify 产生的 category 选分支
                .addConditionalEdges("classify",
                        AsyncEdgeAction.edge_async(state -> state.value("category", "other")),
                        Map.of("tech", "tech_writer",
                                "food", "food_writer",
                                "other", "general_writer"))
                // 三个分支都汇到 END
                .addEdge("tech_writer", StateGraph.END)
                .addEdge("food_writer", StateGraph.END)
                .addEdge("general_writer", StateGraph.END);

        this.compiledGraph = graph.compile();
    }

    /** 执行路由图：返回最终状态（含命中的 category 与对应分支的回答）。 */
    public Map<String, Object> route(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
