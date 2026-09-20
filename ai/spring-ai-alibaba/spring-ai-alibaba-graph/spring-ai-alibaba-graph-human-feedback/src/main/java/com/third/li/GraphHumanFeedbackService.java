package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 人机协同（HITL）：interruptBefore + resume。
 *
 * <p>Graph 支持在指定节点前"暂停等人工"：
 * <ul>
 *   <li>compile 时声明断点 — {@code CompileConfig.builder()
 *       .interruptBefore("human_input").build()}：执行到 human_input 节点前挂起，
 *       状态与执行位置一并存入 checkpoint</li>
 *   <li>检测挂起 — {@code compiledGraph.getState(config).next()} 返回待执行节点名；
 *       非空说明图停在断点上</li>
 *   <li>恢复执行 — {@code RunnableConfig.builder().threadId(同一线程)
 *       .resume().addStateUpdate(Map.of("human_feedback", …)).build()}，
 *       再 invoke：图从断点继续，人工反馈通过状态进入后续节点</li>
 * </ul>
 *
 * <p>流程：propose（LLM 出方案）→【人工挂起】→ human_input（接收反馈）→
 * execute（LLM 按反馈落地）→ END。对照 {@code embabel-hitl} / {@code agentscope-hitl-confirm}。
 */
@Service
public class GraphHumanFeedbackService {

    private final CompiledGraph compiledGraph;

    /** 记录每个线程是否已到断点，供上层判断。 */
    private final Map<String, String> pendingThreads = new ConcurrentHashMap<>();

    public GraphHumanFeedbackService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("hitl_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "proposal", new ReplaceStrategy(),
                        "human_feedback", new ReplaceStrategy(),
                        "result", new ReplaceStrategy()))
                .addNode("propose", node_async(state -> {
                    String input = state.value("input", "");
                    String proposal = chatModel.call(
                            "为下面的需求拟一个 3 条要点的执行方案，只输出方案：\n" + input);
                    return Map.of("proposal", proposal);
                }))
                .addNode("human_input", node_async(state ->
                        // 恢复后从这里继续；人工反馈已在 resume 时写入状态
                        Map.of()))
                .addNode("execute", node_async(state -> {
                    String proposal = state.value("proposal", "");
                    String feedback = state.value("human_feedback", "");
                    String result = chatModel.call(
                            "按人工反馈修订并落地这个方案。\n方案：\n" + proposal + "\n人工反馈：" + feedback);
                    return Map.of("result", result);
                }))
                .addEdge(StateGraph.START, "propose")
                .addEdge("propose", "human_input")
                .addEdge("human_input", "execute")
                .addEdge("execute", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                // 在 human_input 节点前挂起，等待人工反馈
                .interruptBefore("human_input")
                .build();

        this.compiledGraph = graph.compile(compileConfig);
    }

    /** 启动流程：执行到断点挂起，返回当前方案与 threadId。 */
    public Map<String, Object> start(String input) throws Exception {
        String threadId = UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        var result = compiledGraph.invoke(Map.of("input", input), config);
        OverAllState state = result.orElseThrow();
        String next = compiledGraph.getState(config).next();
        if (next != null) {
            pendingThreads.put(threadId, next);
        }
        return Map.of(
                "threadId", threadId,
                "proposal", state.value("proposal", ""),
                "pendingNode", String.valueOf(next));
    }

    /** 提交人工反馈并恢复执行。 */
    public Map<String, Object> resume(String threadId, String feedback) throws Exception {
        if (!pendingThreads.containsKey(threadId)) {
            throw new IllegalStateException("线程 " + threadId + " 不在挂起状态，请先调用 start");
        }
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        // 先把人工反馈写入检查点状态，再从断点恢复
        compiledGraph.updateState(config, Map.of("human_feedback", feedback));
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .resume()
                .build();
        var result = compiledGraph.invoke(Map.of(), resumeConfig);
        pendingThreads.remove(threadId);
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
