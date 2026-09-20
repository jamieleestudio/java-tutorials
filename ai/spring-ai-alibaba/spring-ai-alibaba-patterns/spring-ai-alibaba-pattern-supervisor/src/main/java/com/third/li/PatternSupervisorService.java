package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 主管编排模式（Anthropic Orchestrator-Workers 的 Graph 循环实现）。
 *
 * <p>与 {@code spring-ai-alibaba-agent-multi-agent}（SubAgentInterceptor 工具委派）不同，
 * 这里用<b>图循环</b>实现主管：supervisor 决定下一步交给谁，
 * 条件边把控制权发给对应 worker，worker 完成后回到 supervisor 继续决策，
 * 直到 supervisor 输出 DONE。messages 用 AppendStrategy 累积全程记录。
 *
 * <p>对照 {@code agentscope-supervisor} / {@code embabel-supervisor}：
 * 同一模式，SAA 把"循环 + 分发"画进图里，recursionLimit 兜底防止死循环。
 */
@Service
public class PatternSupervisorService {

    private final CompiledGraph compiledGraph;

    public PatternSupervisorService(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        StateGraph graph = new StateGraph("supervisor_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "decision", new ReplaceStrategy(),
                        "last_result", new ReplaceStrategy(),
                        "final", new ReplaceStrategy(),
                        // 追加策略：全程执行轨迹留存
                        "messages", new AppendStrategy()))
                .addNode("supervisor", node_async(state -> {
                    String history = String.join("\n", state.value("messages", java.util.List.of()));
                    String decision = chatClient.prompt()
                            .system("""
                                    你是任务主管。根据目标与已有进展决定下一步，只输出一行：
                                    PLAN（还没拆解思路）| WRITE（需要写内容）| REVIEW（需要审查）| DONE（已完成）""")
                            .user("目标：" + state.value("input", "") + "\n进展：\n" + history)
                            .call()
                            .content();
                    String d = decision == null ? "DONE" : decision.trim().toUpperCase();
                    if (!d.startsWith("PLAN") && !d.startsWith("WRITE") && !d.startsWith("REVIEW")) {
                        d = "DONE";
                    }
                    return Map.of("decision", d, "messages",
                            java.util.List.of("主管决定: " + d));
                }))
                .addNode("planner", node_async(state -> {
                    String step = chatClient.prompt()
                            .system("把目标拆成 2-3 个执行要点，输出要点列表。")
                            .user(state.value("input", "")).call().content();
                    return Map.of("last_result", step, "messages", java.util.List.of("工人: " + step));
                }))
                .addNode("writer", node_async(state -> {
                    String text = chatClient.prompt()
                            .system("根据目标与要点写一段 80 字以内的正文。目标与要点：")
                            .user(state.value("input", "") + "\n" + state.value("last_result", ""))
                            .call().content();
                    return Map.of("last_result", text, "messages", java.util.List.of("工人: " + text));
                }))
                .addNode("reviewer", node_async(state -> {
                    String review = chatClient.prompt()
                            .system("审查下面的内容，若合格直接给出终稿，否则给出修改稿。")
                            .user(state.value("last_result", "")).call().content();
                    return Map.of("final", review, "messages", java.util.List.of("工人: " + review));
                }))
                .addEdge(StateGraph.START, "supervisor")
                // 主管的条件分发
                .addConditionalEdges("supervisor",
                        AsyncEdgeAction.edge_async(state -> state.value("decision", "DONE")),
                        Map.of("PLAN", "planner",
                                "WRITE", "writer",
                                "REVIEW", "reviewer",
                                "DONE", StateGraph.END))
                // 工人完成后回到主管继续决策（循环）
                .addEdge("planner", "supervisor")
                .addEdge("writer", "supervisor")
                .addEdge("reviewer", "supervisor");

        // recursionLimit 兜底：防止主管永远不输出 DONE
        this.compiledGraph = graph.compile(
                com.alibaba.cloud.ai.graph.CompileConfig.builder().recursionLimit(12).build());
    }

    public Map<String, Object> run(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
