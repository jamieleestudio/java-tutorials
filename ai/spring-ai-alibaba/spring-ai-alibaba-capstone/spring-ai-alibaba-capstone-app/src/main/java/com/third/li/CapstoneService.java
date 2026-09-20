package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 端到端智能客服：一张图串起本教程的核心能力。
 *
 * <pre>
 * START → classify ─┬─ faq ──── knowledge（向量检索 + 生成）
 *                   ├─ order ── order_tool（模拟订单服务 + 生成）
 *                   └─ refund ─ refund_plan ─【挂起等人工】human_confirm ─ finalize ─ END
 * </pre>
 *
 * <ul>
 *   <li>路由 — 结构化分类 + 条件边（graph-conditional）</li>
 *   <li>知识 — 三段式 RAG（rag-basics）</li>
 *   <li>工具 — Java 方法直接作为节点能力（对照 agent-tools 的 @Tool）</li>
 *   <li>HITL — 退款等敏感操作 interruptBefore 挂起，人工确认后 resume（graph-human-feedback）</li>
 * </ul>
 *
 * <p>对照 {@code spring-ai-capstone-app}（Advisor 链版客服）：
 * SAA 用图把"分支 + 工具 + 人工挂起"显式画出来，可 checkpoint、可恢复。
 */
@Service
public class CapstoneService {

    private final CompiledGraph compiledGraph;
    private final Map<String, String> pendingRefunds = new ConcurrentHashMap<>();

    public CapstoneService(ChatModel chatModel, EmbeddingModel embeddingModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.create(chatModel);

        // 知识库：内存向量库 + 预置 FAQ
        var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(List.of(
                new Document("会员权益：基础版 99 元/月，专业版 299 元/月，均可随时取消。", Map.of("k", "faq")),
                new Document("发货时效：现货商品 48 小时内发货，默认顺丰快递。", Map.of("k", "faq")),
                new Document("退款政策：签收后 7 天内可无理由退款，需商品完好。", Map.of("k", "faq"))));
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(2)
                .similarityThreshold(0.0)
                .build();

        StateGraph graph = new StateGraph("support_agent", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "lane", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy(),
                        "order_info", new ReplaceStrategy(),
                        "refund_plan", new ReplaceStrategy(),
                        "human_feedback", new ReplaceStrategy()))
                .addNode("classify", node_async(state -> {
                    String lane = chatClient.prompt()
                            .user("客户请求分类：faq（咨询）/ order（查订单）/ refund（退款）。只输出类名。\n请求："
                                    + state.value("input", ""))
                            .call().content();
                    String laneNorm = lane == null ? "faq" : lane.trim().toLowerCase();
                    if (!laneNorm.startsWith("order") && !laneNorm.startsWith("refund")) {
                        laneNorm = "faq";
                    }
                    return Map.of("lane", laneNorm);
                }))
                // FAQ：RAG
                .addNode("knowledge", node_async(state -> {
                    String input = state.value("input", "");
                    List<Document> docs = retriever.retrieve(new Query(input));
                    String context = docs.stream().map(Document::getText)
                            .reduce("", (a, b) -> a + "\n- " + b);
                    return Map.of("answer", chatClient.prompt()
                            .system("基于上下文回答客服问题，上下文没有的要诚实说明。\n" + context)
                            .user(input).call().content());
                }))
                // 查订单：模拟订单服务（工具即方法）
                .addNode("order_tool", node_async(state -> {
                    String input = state.value("input", "");
                    String orderId = input.replaceAll("\\D", "");
                    String orderInfo = mockOrder(orderId.isEmpty() ? "unknown" : orderId);
                    return Map.of("answer", chatClient.prompt()
                            .system("你是客服，把订单查询结果转述给客户，友好简洁。")
                            .user("客户请求：" + input + "\n订单信息：" + orderInfo)
                            .call().content());
                }))
                // 退款：先出方案
                .addNode("refund_plan", node_async(state -> Map.of("refund_plan",
                        chatClient.prompt()
                                .system("为退款请求拟定处理方案（含核实要点与预计时效），100 字以内。")
                                .user(state.value("input", "")).call().content())))
                // 人工确认：恢复后把反馈并入
                .addNode("human_confirm", node_async(state -> Map.of()))
                .addNode("finalize", node_async(state -> {
                    String plan = state.value("refund_plan", "");
                    String feedback = state.value("human_feedback", "");
                    return Map.of("answer", chatClient.prompt()
                            .system("你是客服主管，按人工审核意见输出最终处理结果给客户。")
                            .user("方案：" + plan + "\n人工意见：" + feedback)
                            .call().content());
                }))
                .addEdge(StateGraph.START, "classify")
                .addConditionalEdges("classify",
                        AsyncEdgeAction.edge_async(state -> state.value("lane", "faq")),
                        Map.of("faq", "knowledge",
                                "order", "order_tool",
                                "refund", "refund_plan"))
                .addEdge("refund_plan", "human_confirm")
                .addEdge("human_confirm", "finalize")
                .addEdge("knowledge", StateGraph.END)
                .addEdge("order_tool", StateGraph.END)
                .addEdge("finalize", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                // 退款必须人工确认后才能继续
                .interruptBefore("human_confirm")
                .build();

        this.compiledGraph = graph.compile(compileConfig);
    }

    /** 客服入口：退款会停在人工确认，其余直接返回答案。 */
    public Map<String, Object> chat(String threadId, String input) throws Exception {
        String tid = (threadId == null || threadId.isBlank()) ? UUID.randomUUID().toString() : threadId;
        RunnableConfig config = RunnableConfig.builder().threadId(tid).build();
        var result = compiledGraph.invoke(Map.of("input", input), config);
        OverAllState state = result.orElseThrow();
        String next = compiledGraph.getState(config).next();
        boolean pending = next != null && !next.isBlank();
        if (pending) {
            pendingRefunds.put(tid, next);
        }
        return Map.of(
                "threadId", tid,
                "lane", state.value("lane", ""),
                "answer", state.value("answer", ""),
                "pendingNode", pending ? next : "");
    }

    /** 人工确认/驳回退款（仅在挂起时有效）。 */
    public Map<String, Object> confirm(String threadId, String feedback) throws Exception {
        if (!pendingRefunds.containsKey(threadId)) {
            throw new IllegalStateException("该会话没有待确认的退款，threadId=" + threadId);
        }
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        // 先把人工审核意见写入检查点状态，再从断点恢复
        compiledGraph.updateState(config, Map.of("human_feedback", feedback));
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .resume()
                .build();
        var result = compiledGraph.invoke(Map.of(), resumeConfig);
        pendingRefunds.remove(threadId);
        return result.map(OverAllState::data).orElse(Map.of());
    }

    private String mockOrder(String orderId) {
        return switch (orderId) {
            case "1001" -> "订单 1001：已发货，顺丰 SF1234567890，预计明日达";
            case "1002" -> "订单 1002：已签收";
            default -> "订单 " + orderId + "：未找到";
        };
    }
}
