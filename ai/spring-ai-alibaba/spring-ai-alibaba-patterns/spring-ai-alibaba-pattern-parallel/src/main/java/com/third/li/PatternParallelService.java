package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 并行模式（Anthropic Parallelization 的 Graph 扇出/扇入实现）。
 *
 * <p>Graph 原生支持并行：
 * <ul>
 *   <li>扇出 — {@code addEdge("router", List.of("a", "b", "c"))}：
 *       一个节点完成后并发推进多个下游节点</li>
 *   <li>扇入 — {@code addEdge(List.of("a", "b", "c"), "merge")}：
 *       所有分支都完成后才进入汇合节点（隐式 barrier）</li>
 * </ul>
 *
 * <p>与 {@code agentscope-parallelization}（Flux.merge）、
 * {@code spring-ai-parallelization}（手写 Flux.merge）对照：
 * Graph 把并行拓扑写进图里，分支间无需手动编排线程/流。
 */
@Service
public class PatternParallelService {

    private final CompiledGraph compiledGraph;

    public PatternParallelService(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        StateGraph graph = new StateGraph("review_panel", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "review_tech", new ReplaceStrategy(),
                        "review_biz", new ReplaceStrategy(),
                        "review_ux", new ReplaceStrategy(),
                        "verdict", new ReplaceStrategy()))
                .addNode("dispatch", node_async(state -> Map.of()))
                // 三个并行评审员：各写各的状态 key
                .addNode("tech_reviewer", node_async(state -> Map.of("review_tech",
                        chatClient.prompt().system("你是技术评审，一句话点评可行性。")
                                .user(state.value("input", "")).call().content())))
                .addNode("biz_reviewer", node_async(state -> Map.of("review_biz",
                        chatClient.prompt().system("你是业务评审，一句话点评商业价值。")
                                .user(state.value("input", "")).call().content())))
                .addNode("ux_reviewer", node_async(state -> Map.of("review_ux",
                        chatClient.prompt().system("你是体验评审，一句话点评用户体验。")
                                .user(state.value("input", "")).call().content())))
                // 汇合节点：三份意见都到齐才执行
                .addNode("merge", node_async(state -> {
                    String all = "技术：" + state.value("review_tech", "") + "\n"
                            + "业务：" + state.value("review_biz", "") + "\n"
                            + "体验：" + state.value("review_ux", "");
                    return Map.of("verdict", chatClient.prompt()
                            .system("综合三位评审的意见给出最终结论，两句话以内。")
                            .user(all).call().content());
                }))
                .addEdge(StateGraph.START, "dispatch")
                // 扇出：dispatch → 三个评审员并发
                .addEdge("dispatch", java.util.List.of("tech_reviewer", "biz_reviewer", "ux_reviewer"))
                // 扇入：三个评审员 → merge
                .addEdge(java.util.List.of("tech_reviewer", "biz_reviewer", "ux_reviewer"), "merge")
                .addEdge("merge", StateGraph.END);

        this.compiledGraph = graph.compile();
    }

    public Map<String, Object> run(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
