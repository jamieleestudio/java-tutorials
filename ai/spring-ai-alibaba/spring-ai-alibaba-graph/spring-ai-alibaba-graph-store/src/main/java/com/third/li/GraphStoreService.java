package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.NamespaceListRequest;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 长期记忆 Store：跨 thread 的命名空间键值存储。
 *
 * <p>checkpoint（graph-checkpoint 模块）解决"会话内状态可恢复"，
 * {@link Store} 解决"跨会话的长期记忆"：
 * <ul>
 *   <li>{@code CompileConfig.builder().store(...)} 挂载存储
 *       （内置 {@link MemoryStore} / FileSystemStore / RedisStore / MongoStore 等）</li>
 *   <li>节点里通过 {@code config.store()} 拿到 Store，按命名空间（List 形式的层级键）
 *       putItem / getItem / searchItems</li>
 *   <li>命名空间通常带 userId / threadId 维度，实现"按用户记住偏好"</li>
 * </ul>
 */
@Service
public class GraphStoreService {

    private final CompiledGraph compiledGraph;
    private final Store store = new MemoryStore();

    public GraphStoreService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("store_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                .addNode("memory", AsyncNodeActionWithConfig.node_async((state, config) -> {
                    String input = state.value("input", "");
                    String threadId = config.threadId().orElse("anonymous");
                    List<String> namespace = List.of("users", threadId);
                    if (input.startsWith("记住:")) {
                        // 写入长期记忆：key=偏好名 value=内容
                        String[] kv = input.substring(3).split("=", 2);
                        String key = kv[0].trim();
                        String value = kv.length > 1 ? kv[1].trim() : "";
                        store.putItem(StoreItem.of(namespace, key, Map.of("content", value)));
                        return Map.of("answer", "已记住：" + key + " = " + value);
                    }
                    // 读取长期记忆
                    String key = input.replaceFirst("^回忆[:：]?", "").trim();
                    var item = store.getItem(namespace, key);
                    String answer = item.map(i -> String.valueOf(i.getValue().get("content")))
                            .orElse("我没有关于「" + key + "」的记忆");
                    return Map.of("answer", answer);
                }))
                .addEdge(StateGraph.START, "memory")
                .addEdge("memory", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .store(store)
                .build();

        this.compiledGraph = graph.compile(compileConfig);
    }

    /** 在指定会话里读写长期记忆；threadId 为空则新建。 */
    public Map<String, Object> chat(String threadId, String input) throws Exception {
        String tid = (threadId == null || threadId.isBlank()) ? UUID.randomUUID().toString() : threadId;
        RunnableConfig config = RunnableConfig.builder().threadId(tid).build();
        var result = compiledGraph.invoke(Map.of("input", input), config);
        Map<String, Object> data = new java.util.HashMap<>(result.map(OverAllState::data).orElse(Map.of()));
        data.put("threadId", tid);
        return data;
    }

    /** 直接查看 Store 里的命名空间（观察跨 thread 的存储内容）。 */
    public String dump() {
        var namespaces = store.listNamespaces(NamespaceListRequest.builder().build());
        return "Store 条目数: " + store.size() + "，命名空间: " + namespaces;
    }
}
