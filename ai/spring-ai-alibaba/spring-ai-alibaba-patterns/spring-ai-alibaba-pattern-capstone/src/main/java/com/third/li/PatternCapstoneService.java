package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 模式综合：路由 + 并行 + 自评迭代，一张图串起三种编排模式。
 *
 * <p>拓扑：
 * <pre>
 *   START → router ─┬─(story)→ 比喻写手 ─┐
 *                   │                    ├─ merge → refine(PASS/RETRY 自环，最多 2 轮) → END
 *                   └─(answer)→ answer ─────────────────────────────────────────────┘
 *                             (answer 分支汇入 refine 前的 END)
 * </pre>
 *
 * <p>对照 {@code spring-ai-capstone-patterns} / {@code agentscope-capstone-patterns}：
 * 综合模式的价值在于验证"多种控制流在同一个图里共存"——
 * 这正是 Graph 相对手写编排的最大优势。
 */
@Service
public class PatternCapstoneService {

    private final CompiledGraph compiledGraph;

    public PatternCapstoneService(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        StateGraph graph = new StateGraph("patterns_capstone", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "route", new ReplaceStrategy(),
                        "draft_metaphor", new ReplaceStrategy(),
                        "draft_plain", new ReplaceStrategy(),
                        "merged", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy(),
                        "refined", new ReplaceStrategy(),
                        "verdict", new ReplaceStrategy(),
                        "rounds", new ReplaceStrategy()))
                // 路由
                .addNode("router", node_async(state -> {
                    String route = chatClient.prompt()
                            .user("任务分类：story（写一段有画面感的文字）或 answer（回答一个事实问题）。只输出类名。\n任务："
                                    + state.value("input", ""))
                            .call().content();
                    String r = route == null ? "answer" : route.trim().toLowerCase();
                    if (!r.startsWith("story") && !r.startsWith("answer")) {
                        r = "answer";
                    }
                    return Map.of("route", r);
                }))
                // story 分支：并行双写手
                .addNode("metaphor_writer", node_async(state -> Map.of("draft_metaphor",
                        chatClient.prompt().system("多用比喻，写得有画面感。")
                                .user(state.value("input", "")).call().content())))
                .addNode("plain_writer", node_async(state -> Map.of("draft_plain",
                        chatClient.prompt().system("白描风格，简洁克制。")
                                .user(state.value("input", "")).call().content())))
                .addNode("merge", node_async(state -> {
                    String merged = chatClient.prompt()
                            .system("把两份草稿的优点合成一份终稿，只输出终稿。")
                            .user("草稿A：" + state.value("draft_metaphor", "")
                                    + "\n草稿B：" + state.value("draft_plain", ""))
                            .call().content();
                    return Map.of("merged", merged, "rounds", 1);
                }))
                // answer 分支：直接回答
                .addNode("answer", node_async(state -> Map.of("refined",
                        chatClient.prompt().user(state.value("input", "")).call().content(),
                        "verdict", "PASS")))
                // story 分支的调度节点（扇出用）
                .addNode("dispatch", node_async(state -> Map.of()))
                // 迭代打磨：自评分，RETRY 则自环重写（最多 2 轮，recursionLimit 兜底）
                .addNode("refine", node_async(state -> {
                    String draft = state.value("merged", "");
                    int rounds = state.value("rounds", 1);
                    String verdict = chatClient.prompt()
                            .system("给下面文字打分（0-10），并输出格式：PASS 或 RETRY|修改稿。若 ≥8 分输出 PASS。")
                            .user(draft).call().content();
                    if (rounds >= 2 || (verdict != null && verdict.contains("PASS"))) {
                        return Map.of("refined", draft, "verdict", "PASS");
                    }
                    String improved = verdict == null ? draft : verdict.replaceFirst("(?i)RETRY\\|?", "");
                    return Map.of("merged", improved, "rounds", rounds + 1, "verdict", "RETRY");
                }))
                .addEdge(StateGraph.START, "router")
                .addConditionalEdges("router",
                        AsyncEdgeAction.edge_async(state -> state.value("route", "answer")),
                        Map.of("story", "dispatch", "answer", "answer"))
                // story 分支：调度节点扇出双写手
                .addEdge("dispatch", List.of("metaphor_writer", "plain_writer"))
                .addEdge(List.of("metaphor_writer", "plain_writer"), "merge")
                // 打磨自环
                .addConditionalEdges("refine",
                        AsyncEdgeAction.edge_async(state -> state.value("verdict", "PASS")),
                        Map.of("PASS", StateGraph.END, "RETRY", "refine"))
                .addEdge("answer", StateGraph.END);

        this.compiledGraph = graph.compile(
                CompileConfig.builder().recursionLimit(10).build());
    }

    public Map<String, Object> run(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
