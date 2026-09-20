package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.InterruptableActionWithConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 工具级 HITL：敏感工具执行前挂起等待人工审批。
 *
 * <p>机制（与节点级 HITL 同源，粒度到"某次工具调用"）：
 * <ol>
 *   <li>compile 时声明 {@code interruptBefore("send_email")}：图在工具节点前挂起，
 *       状态与执行位置落入 checkpoint</li>
 *   <li>工具节点实现 {@link InterruptableActionWithConfig}：{@code interrupt(...)}
 *       返回 {@link InterruptionMetadata}（含 {@code ToolFeedback} 描述待审批的工具调用），
 *       供审批方展示"将要执行什么"</li>
 *   <li>审批结果经 {@code updateState} 写入状态，{@code resume()} 恢复 ——
 *       节点读到 {@code approval_result} 后决定真正执行还是驳回</li>
 * </ol>
 *
 * <p>这就是 SAA 版的"发送邮件前请人工确认"。Agent Framework 的
 * {@code HumanInTheLoopHook}（approvalOn + HumanInteractionHandler）内部是同一套机制。
 */
@Service
public class GraphToolHitlService {

    private final CompiledGraph compiledGraph;

    /** threadId → 待审批的工具调用描述（InterruptionMetadata.ToolFeedback）。 */
    private final Map<String, InterruptionMetadata.ToolFeedback> pending = new ConcurrentHashMap<>();

    public GraphToolHitlService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("tool_hitl_graph", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "email_draft", new ReplaceStrategy(),
                        "approval_result", new ReplaceStrategy(),
                        "send_result", new ReplaceStrategy()))
                .addNode("draft", node_async(state -> {
                    String draft = chatModel.call(
                            "为下面的请求写一封 60 字以内的通知邮件草稿，只输出正文：\n"
                                    + state.value("input", ""));
                    return Map.of("email_draft", draft);
                }))
                .addNode("send_email", new ApprovalNode())
                .addEdge(StateGraph.START, "draft")
                .addEdge("draft", "send_email")
                .addEdge("send_email", StateGraph.END);

        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                // 工具节点前挂起，等人工审批
                .interruptBefore("send_email")
                .build();

        this.compiledGraph = graph.compile(compileConfig);
    }

    /** 工具节点：读审批结果决定发送还是驳回；支持 InterruptableAction 元数据。 */
    private class ApprovalNode implements AsyncNodeActionWithConfig, InterruptableActionWithConfig {

        @Override
        public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
            String threadId = config.threadId().orElse("unknown");
            String approval = state.value("approval_result", "");
            String draft = state.value("email_draft", "");
            String result = "approved".equalsIgnoreCase(approval)
                    ? "邮件已发送（收件人 mock@demo.dev）：" + draft
                    : "邮件发送已被驳回（审批结果：" + approval + "），草稿保留：" + draft;
            return CompletableFuture.completedFuture(Map.of("send_result", result));
        }

        @Override
        public java.util.Optional<InterruptionMetadata> interrupt(
                String nodeId, OverAllState state, RunnableConfig config) {
            String threadId = config.threadId().orElse("unknown");
            // 恢复执行时 config 会携带人工反馈元数据（若有）
            var feedbackOpt = config.metadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY);
            if (feedbackOpt.isPresent() && feedbackOpt.get() instanceof InterruptionMetadata metadata
                    && !metadata.toolFeedbacks().isEmpty()) {
                pending.put(threadId, metadata.toolFeedbacks().get(0));
                return java.util.Optional.empty();
            }
            // 首次进入 → 生成待审批的"工具调用"描述
            InterruptionMetadata.ToolFeedback pendingCall = InterruptionMetadata.ToolFeedback.builder()
                    .id(UUID.randomUUID().toString())
                    .name("send_email")
                    .arguments("{\"to\":\"mock@demo.dev\"}")
                    .description("即将发送邮件：" + state.value("email_draft", ""))
                    .build();
            pending.put(threadId, pendingCall);
            return java.util.Optional.of(InterruptionMetadata.builder(nodeId, state)
                    .addToolFeedback(pendingCall)
                    .build());
        }
    }

    /** 发起"发邮件"流程：执行到工具节点前挂起，返回草稿与 threadId。 */
    public Map<String, Object> start(String input) throws Exception {
        String threadId = UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        var result = compiledGraph.invoke(Map.of("input", input), config);
        OverAllState state = result.orElseThrow();
        String next = compiledGraph.getState(config).next();
        boolean suspended = next != null && !next.isBlank();
        // interrupt() 若被调用会登记 ToolFeedback；展示给审批方
        return Map.of(
                "threadId", threadId,
                "email_draft", state.value("email_draft", ""),
                "pending", suspended,
                "pendingNode", String.valueOf(next),
                "toolCall", pending.containsKey(threadId)
                        ? pending.get(threadId).getName() + ": " + pending.get(threadId).getDescription()
                        : "send_email（工具调用待审批）");
    }

    /** 审批：approved=true 放行发送，false 驳回。 */
    public Map<String, Object> approve(String threadId, boolean approved) throws Exception {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        // 审批结果写入检查点状态，再从断点恢复执行工具节点
        compiledGraph.updateState(config, Map.of("approval_result", approved ? "approved" : "rejected"));
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .resume()
                .build();
        var result = compiledGraph.invoke(Map.of(), resumeConfig);
        OverAllState state = result.orElseThrow();
        return Map.of(
                "approved", approved,
                "send_result", state.value("send_result", ""));
    }
}
