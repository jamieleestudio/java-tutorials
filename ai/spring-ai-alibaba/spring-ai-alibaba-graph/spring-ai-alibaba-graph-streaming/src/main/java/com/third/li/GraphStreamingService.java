package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 图级流式输出：compiledGraph.stream() 逐节点事件。
 *
 * <p>Graph 的 stream() 与 invoke() 的差别：
 * <ul>
 *   <li>{@code invoke(inputs)} — 等整张图跑完，一次性拿最终状态</li>
 *   <li>{@code stream(inputs)} — 返回 {@code Flux<NodeOutput>}，每个节点执行完
 *       立即推送一个事件（含节点名与当时的状态），工作流中间产物实时可见</li>
 * </ul>
 *
 * <p>说明：M1.1 里程碑版中，普通节点内的 LLM token 级直通流式
 * （1.x 的 AsyncGenerator + StreamingOutput 模式）尚未开放，
 * token 级流式建议直接用 ChatModel/ChatClient（见 spring-ai-alibaba-streaming），
 * 或等待 GA 版本的图内流式支持。
 */
@Service
public class GraphStreamingService {

    private final CompiledGraph compiledGraph;

    public GraphStreamingService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("streaming_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "outline", new ReplaceStrategy(),
                        "article", new ReplaceStrategy()))
                // 节点 1：先出提纲
                .addNode("outline", node_async(state -> {
                    String outline = chatModel.call(
                            "为下面的主题拟一个 2 条要点的提纲，只输出提纲：" + state.value("input", ""));
                    return Map.of("outline", outline);
                }))
                // 节点 2：按提纲成文
                .addNode("writer", node_async(state -> {
                    String article = chatModel.call(
                            "按提纲写一段 80 字左右的短文：\n" + state.value("outline", ""));
                    return Map.of("article", article);
                }))
                .addEdge(StateGraph.START, "outline")
                .addEdge("outline", "writer")
                .addEdge("writer", StateGraph.END);

        this.compiledGraph = graph.compile();
    }

    /** 节点级流式：每个节点完成即推送 [节点名] + 该节点写入状态的内容。 */
    public Flux<String> stream(String input) {
        return compiledGraph
                .stream(Map.of("input", input))
                .map(this::render);
    }

    private String render(NodeOutput output) {
        String node = output.node();
        OverAllState state = output.state();
        StringBuilder sb = new StringBuilder("[" + node + "]");
        if (state != null) {
            state.value("outline").ifPresent(v -> sb.append(" 提纲：").append(v));
            state.value("article").ifPresent(v -> sb.append(" 成文：").append(v));
        }
        return sb.toString();
    }
}
