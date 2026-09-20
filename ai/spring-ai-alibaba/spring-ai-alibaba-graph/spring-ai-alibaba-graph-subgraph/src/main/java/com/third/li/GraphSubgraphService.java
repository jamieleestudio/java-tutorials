package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 子图组合：把整张图作为一个节点嵌入父图。
 *
 * <p>{@code addNode("子图节点名", subGraph)} 接受一个 {@link StateGraph} 或
 * {@link CompiledGraph} 作为节点 —— 子图内部有自己的 START/END 和状态流，
 * 对父图而言只是一个普通节点。这是把"可复用工作流"封装成积木的方式，
 * 也是多 Agent 系统里"Agent-as-Node"的基础
 * （ReactAgent 内部就是一张图，通过同样的机制接入更大的图）。
 *
 * <p>父图：prepare（整理要点）→ translateSub（子图：翻译）→ polish（润色）。
 */
@Service
public class GraphSubgraphService {

    private final CompiledGraph parentGraph;

    public GraphSubgraphService(ChatModel chatModel) throws GraphStateException {
        // 子图：一句直译 → 一句意译，输出合并
        StateGraph translateSub = new StateGraph("translate_sub", () -> Map.of(
                        "text", new ReplaceStrategy(),
                        "translated", new ReplaceStrategy()))
                .addNode("literal", node_async(state -> Map.of("translated",
                        chatModel.call("把下面的文字直译成英文，只输出译文：\n" + state.value("text", "")))))
                .addNode("idiomatic", node_async(state -> Map.of("translated",
                        chatModel.call("把下面的文字意译成地道的英文，只输出译文：\n" + state.value("text", "")))))
                .addEdge(StateGraph.START, "literal")
                .addEdge("literal", "idiomatic")
                .addEdge("idiomatic", StateGraph.END);

        // 父图：把子图当作一个节点
        StateGraph parent = new StateGraph("parent_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "text", new ReplaceStrategy(),
                        "translated", new ReplaceStrategy(),
                        "result", new ReplaceStrategy()))
                .addNode("prepare", node_async(state -> {
                    String text = chatModel.call(
                            "把下面这句话整理成一句通顺的中文，只输出整理后的句子：\n" + state.value("input", ""));
                    return Map.of("text", text);
                }))
                // 直接挂子图
                .addNode("translate", translateSub)
                .addNode("polish", node_async(state -> Map.of("result",
                        chatModel.call("选一个更好的译文并给出理由：\n" + state.value("translated", "")))))
                .addEdge(StateGraph.START, "prepare")
                .addEdge("prepare", "translate")
                .addEdge("translate", "polish")
                .addEdge("polish", StateGraph.END);

        this.parentGraph = parent.compile();
    }

    /** 执行父图（内嵌子图）。 */
    public Map<String, Object> run(String input) throws Exception {
        var result = parentGraph.invoke(Map.of("input", input));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
