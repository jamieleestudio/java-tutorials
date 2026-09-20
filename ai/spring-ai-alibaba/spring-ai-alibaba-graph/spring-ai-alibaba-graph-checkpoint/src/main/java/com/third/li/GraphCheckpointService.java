package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 状态 checkpoint：MemorySaver + threadId + 状态历史。
 *
 * <p>Graph 的执行状态可以随时落到 CheckpointSaver，实现"可恢复的图执行"：
 * <ul>
 *   <li>compile 时挂载 saver — {@code CompileConfig.builder()
 *       .saverConfig(SaverConfig.builder().register(new MemorySaver()).build()).build()}；
 *       生产可用 MysqlSaver / RedisSaver / PostgresSaver 等（同包下）</li>
 *   <li>执行时带 threadId — {@code RunnableConfig.builder().threadId(id).build()}，
 *       同一 threadId 的多次执行共享一份状态</li>
 *   <li>state key 用 {@code AppendStrategy} 时，跨多次执行会<b>追加</b>而非覆盖，
 *       天然适合多轮对话记忆（messages 越聊越长）</li>
 *   <li>{@code getStateHistory(config)} 可回看该线程每一步落下的快照</li>
 * </ul>
 */
@Service
public class GraphCheckpointService {

    private final CompiledGraph compiledGraph;

    public GraphCheckpointService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("memory_graph", () -> Map.of(
                        // messages 追加：多轮执行的历史都留存在状态里
                        "messages", new AppendStrategy(),
                        "input", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                .addNode("chat", node_async(state -> {
                    String input = state.value("input", "");
                    List<String> history = state.value("messages", List.of());
                    String answer;
                    if (history.isEmpty()) {
                        answer = chatModel.call("回答用户问题：" + input);
                    } else {
                        // 只取最近的历史片段，避免上下文过长
                        String context = String.join("\n", history);
                        if (context.length() > 800) {
                            context = context.substring(context.length() - 800);
                        }
                        answer = chatModel.call(
                                "结合之前的对话历史简短回答：\n" + context + "\n用户：" + input);
                    }
                    return Map.of("messages", List.of("用户: " + input, "助手: " + answer), "answer", answer);
                }))
                .addEdge(StateGraph.START, "chat")
                .addEdge("chat", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .build();

        this.compiledGraph = graph.compile(compileConfig);
    }

    /** 在指定线程上执行一轮对话；threadId 为空则新建一个。 */
    public Map<String, Object> chat(String threadId, String input) throws Exception {
        String tid = (threadId == null || threadId.isBlank()) ? UUID.randomUUID().toString() : threadId;
        RunnableConfig config = RunnableConfig.builder().threadId(tid).build();
        var result = compiledGraph.invoke(Map.of("input", input), config);
        Map<String, Object> data = result.map(OverAllState::data).orElse(Map.of());
        data.put("threadId", tid);
        return data;
    }

    /** 回看线程的状态快照历史（每步执行后落下的 checkpoint）。 */
    public List<String> history(String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        return compiledGraph.getStateHistory(config).stream()
                .map(snapshot -> "next=" + snapshot.next() + " | state=" + snapshot.state().data())
                .toList();
    }
}
