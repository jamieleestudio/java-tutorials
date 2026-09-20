package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.file.FileSystemSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * checkpoint 落盘：FileSystemSaver 持久化与恢复。
 *
 * <p>{@code MemorySaver} 的 checkpoint 只在内存里，进程重启即失。
 * {@link FileSystemSaver} 把每个 checkpoint 序列化成文件落盘（target 目录下按
 * threadId/checkpointId 组织），进程重启后同一 saver 目录 + 同一 threadId 即可续接历史：
 * <ul>
 *   <li>编译时挂载 {@code FileSystemSaver.builder().targetFolder(dir).build()}</li>
 *   <li>本模块演示"模拟重启"：每次 {@code restart()} 都用同一个磁盘目录
 *       新建 CompiledGraph 实例 —— 新实例依然能看到旧实例留下的状态历史</li>
 *   <li>生产环境换 MysqlSaver / RedisSaver / PostgresSaver / MongoSaver（同包）</li>
 * </ul>
 */
@Service
public class GraphFileSaverService {

    private final ChatModel chatModel;
    private final Path checkpointDir;

    public GraphFileSaverService(ChatModel chatModel) throws IOException {
        this.chatModel = chatModel;
        this.checkpointDir = Path.of("demo-checkpoints").toAbsolutePath();
        Files.createDirectories(checkpointDir);
    }

    /** 用磁盘上的 checkpoint 目录构建一个新的 CompiledGraph（等价于一次"重启"）。 */
    private CompiledGraph buildGraph() throws GraphStateException {
        StateGraph graph = new StateGraph("persistent_graph", () -> Map.of(
                        "messages", new AppendStrategy(),
                        "input", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                .addNode("chat", node_async(state -> {
                    String input = state.value("input", "");
                    List<String> history = state.value("messages", List.of());
                    String context = String.join("\n", history);
                    String answer = history.isEmpty()
                            ? chatModel.call("回答用户问题：" + input)
                            : chatModel.call("结合历史简短回答：\n" + context + "\n用户：" + input);
                    return Map.of("messages", List.of("用户: " + input, "助手: " + answer), "answer", answer);
                }))
                .addEdge(StateGraph.START, "chat")
                .addEdge("chat", StateGraph.END);

        FileSystemSaver saver = FileSystemSaver.builder()
                .targetFolder(checkpointDir)
                .build();

        return graph.compile(CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .build());
    }

    /** 在同一 thread 上执行一轮（threadId 为空则新建）。 */
    public Map<String, Object> chat(String threadId, String input) throws Exception {
        String tid = (threadId == null || threadId.isBlank()) ? UUID.randomUUID().toString() : threadId;
        // 每次都新建图实例：证明状态来自磁盘而不是内存
        CompiledGraph compiled = buildGraph();
        RunnableConfig config = RunnableConfig.builder().threadId(tid).build();
        var result = compiled.invoke(Map.of("input", input), config);
        Map<String, Object> data = new java.util.HashMap<>(result.map(OverAllState::data).orElse(Map.of()));
        data.put("threadId", tid);
        data.put("checkpointFiles", countCheckpoints(tid));
        return data;
    }

    /** 回看磁盘 checkpoint：新图实例也能读到完整历史。 */
    public String history(String threadId) throws Exception {
        CompiledGraph compiled = buildGraph();
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        List<String> lines = compiled.getStateHistory(config).stream()
                .map(s -> "next=" + s.next() + " | messages=" + s.state().value("messages", List.of()).size())
                .toList();
        return "磁盘 checkpoint 文件数: " + countCheckpoints(threadId) + "\n" + String.join("\n", lines);
    }

    private long countCheckpoints(String threadId) {
        // FileSystemSaver 的落盘结构：<dir>/thread-<threadId>.saver
        Path file = checkpointDir.resolve("thread-" + threadId + ".saver");
        return Files.exists(file) ? 1 : 0;
    }
}
