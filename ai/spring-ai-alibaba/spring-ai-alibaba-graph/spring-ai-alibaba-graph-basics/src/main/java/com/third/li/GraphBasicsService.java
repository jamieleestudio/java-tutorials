package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Graph 编排基础：StateGraph + 节点 + 边。
 *
 * <p>Spring AI Alibaba 的 Graph 框架（源自 LangGraph 思想）用<b>图</b>描述 Agent 工作流：
 * <ul>
 *   <li>{@link StateGraph} — 图定义：{@code new StateGraph(KeyStrategyFactory)}，
 *       KeyStrategyFactory 声明每个状态 key 的合并策略（Replace/Append/Merge）</li>
 *   <li>节点 — {@code addNode(name, node_async(NodeAction))}，NodeAction 读 {@link OverAllState}
 *       返回要合并进状态的数据；{@code node_async} 把同步动作包装成异步动作</li>
 *   <li>边 — {@code addEdge(START, "a")}、{@code addEdge("b", END)} 串起执行路径；
 *       START/END 是图内置的虚拟节点</li>
 *   <li>{@link CompiledGraph} — {@code graph.compile()} 后可 {@code invoke(输入)} 执行整个图，
 *       返回最终 {@link OverAllState}</li>
 * </ul>
 *
 * <p>与 {@code ai/spring-ai/spring-ai-patterns/spring-ai-prompt-chaining}（手写链条）对照：
 * Graph 把"链"变成了可声明、可可视化、可中断恢复的一等公民。
 */
@Service
public class GraphBasicsService {

    private final CompiledGraph compiledGraph;

    public GraphBasicsService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("writing_graph", () -> Map.of(
                        // 每个状态 key 的合并策略：新值直接覆盖旧值
                        "input", new ReplaceStrategy(),
                        "keywords", new ReplaceStrategy(),
                        "article", new ReplaceStrategy()))
                // 节点 1：提取关键词
                .addNode("analyze", node_async(state -> {
                    String input = state.value("input", "");
                    String keywords = chatModel.call(
                            "从下面的文本提取 3 个关键词，用顿号分隔，只输出关键词：\n" + input);
                    return Map.of("keywords", keywords);
                }))
                // 节点 2：围绕关键词扩写成短文
                .addNode("expand", node_async(state -> {
                    String keywords = state.value("keywords", "");
                    String article = chatModel.call(
                            "围绕关键词写一段 100 字左右的中文短文：" + keywords);
                    return Map.of("article", article);
                }))
                // 节点 3：润色收尾
                .addNode("polish", node_async(state -> {
                    String article = state.value("article", "");
                    String polished = chatModel.call(
                            "润色下面的文字，保持原意，输出润色后的结果：\n" + article);
                    return Map.of("article", polished);
                }))
                // 边：START → analyze → expand → polish → END
                .addEdge(StateGraph.START, "analyze")
                .addEdge("analyze", "expand")
                .addEdge("expand", "polish")
                .addEdge("polish", StateGraph.END);

        this.compiledGraph = graph.compile();
    }

    /** 执行整张图：输入文本 → 关键词 → 短文 → 润色稿。 */
    public Map<String, Object> run(String input) throws Exception {
        var result = compiledGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }

    /** 输出 Mermaid 图定义，可直接粘贴到 Mermaid Live Editor 可视化。 */
    public String mermaid() {
        GraphRepresentation representation =
                compiledGraph.getGraph(GraphRepresentation.Type.MERMAID, "writing_graph");
        return representation.content();
    }
}
