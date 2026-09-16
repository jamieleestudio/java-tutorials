package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.api.tool.ArtifactSink;
import com.embabel.agent.api.tool.CompositeSink;
import com.embabel.agent.api.tool.ListSink;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具链式展开接口 + artifact sink 的显式用法。
 */
@RestController
public class ToolChainingController {

    private final AgentPlatform agentPlatform;

    public ToolChainingController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 主演示：工具产出 Order → Order 的专属工具解锁 → 继续调用完成请求。 */
    @GetMapping("/tool-chaining/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "查一下订单 A1001，然后给它打 9 折") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }

    /**
     * artifact sink 的显式用法：把工具的产物按**类型过滤 → 谓词过滤 → 转换 → 分发到多个 sink**。
     *
     * <p>这里不需要 Agent 进程（不落黑板），所以直接在控制器里调用即可：
     * {@code Tool.sinkArtifacts(tool, clazz, sink, filter, transform)}。
     */
    @GetMapping("/artifacts/sink")
    public Map<String, Object> sink() {
        ListSink listSink = new ListSink();
        List<String> auditLog = new ArrayList<>();
        ArtifactSink auditSink = artifact -> auditLog.add("审计：" + artifact);

        Tool raw = Tool.create("search_orders", "返回一批订单（含一个非 Order 的干扰项）", args ->
                Tool.Result.withArtifact("命中 3 条", List.of(
                        new Order("A1001", "已支付"),
                        new Order("A1002", "待支付"),
                        "not-an-order")));

        // 类过滤（只收 Order）→ 谓词过滤（只收已支付）→ 转换（变成摘要字符串）→ 同时进两个 sink
        Tool wrapped = Tool.sinkArtifacts(
                raw,
                Order.class,
                new CompositeSink(listSink, auditSink),
                order -> "已支付".equals(order.getStatus()),
                order -> order.getId() + "/" + order.getStatus());

        Tool.Result result = wrapped.call("{}");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("toolContent", result instanceof Tool.Result.WithArtifact withArtifact
                ? withArtifact.getContent()
                : result.toString());
        out.put("capturedByListSink", listSink.items());
        out.put("auditLog", auditLog);
        out.put("note", "原始 artifact 是 3 个元素（2 个 Order + 1 个 String）；"
                + "A1002 因状态为「待支付」被谓词过滤，String 因类型不符被过滤，最终只捕获 1 条");
        return out;
    }
}
