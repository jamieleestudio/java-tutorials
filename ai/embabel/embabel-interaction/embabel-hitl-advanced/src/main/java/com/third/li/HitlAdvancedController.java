package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.hitl.Awaitable;
import com.embabel.agent.core.hitl.ResponseImpact;
import com.embabel.agent.core.hitl.TypeRequest;
import com.embabel.agent.core.hitl.TypeResponse;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工具级 HITL 接口。
 *
 * <p>流程与 {@code embabel-hitl} 一致：启动 → 若 {@code WAITING} 就返回 processId 与"需要什么"
 * → 前端补充输入 → 恢复。
 *
 * <p>恢复时用的是**响应对象协议**（而不是直接往黑板塞对象）：
 * 构造 {@link TypeResponse} 并调用 {@code awaitable.onResponse(response, process)}，
 * 由 awaitable 自己决定如何更新进程状态（返回 {@link ResponseImpact}）。
 */
@RestController
@RequestMapping("/hitl-advanced")
public class HitlAdvancedController {

    private final AgentPlatform agentPlatform;

    public HitlAdvancedController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 启动退款流程。小额会自动完成，大额会停在 WAITING 等账号信息。 */
    @GetMapping("/refund")
    public Map<String, Object> refund(
            @RequestParam(value = "message", defaultValue = "退款订单 A1001 金额 5000") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, RefundOutcome.class)
                .run(new UserInput(message));
        return describe(process);
    }

    /** 提交退款账号信息，恢复流程。 */
    @PostMapping("/{processId}/account")
    public Map<String, Object> provideAccount(
            @PathVariable String processId,
            @RequestBody RefundAccount account) {
        AgentProcess process = agentPlatform.getAgentProcess(processId);
        if (process == null) {
            return Map.of("status", "NOT_FOUND", "processId", processId);
        }
        Object lastResult = process.lastResult();
        if (!(lastResult instanceof Awaitable<?, ?> awaitable)) {
            return Map.of("status", "NOT_WAITING", "processId", processId);
        }
        ResponseImpact impact = respondWithValue(awaitable, account, process);
        process.run();
        Map<String, Object> result = describe(process);
        result.put("responseImpact", impact.name());
        return result;
    }

    /**
     * 把用户提供的值交回给 awaitable。
     *
     * <p>{@link TypeRequest} 的 payload 是"要什么类型"，响应是 {@link TypeResponse}；
     * 它的 {@code onResponse} 会把用户提供的值加进黑板（{@code ResponseImpact.UPDATED}）。
     */
    @SuppressWarnings("unchecked")
    private ResponseImpact respondWithValue(Awaitable<?, ?> awaitable, Object value, AgentProcess process) {
        Awaitable<Object, TypeResponse<Object>> typed =
                (Awaitable<Object, TypeResponse<Object>>) awaitable;
        TypeResponse<Object> response = new TypeResponse<>(
                value,
                typed.getId(),
                UUID.randomUUID().toString(),
                Instant.now(),
                false);
        return typed.onResponse(response, process);
    }

    private Map<String, Object> describe(AgentProcess process) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processId", process.getId());
        result.put("status", process.getStatus().name());

        if (process.getStatus() == AgentProcessStatusCode.WAITING
                && process.lastResult() instanceof Awaitable<?, ?> awaitable) {
            result.put("awaitableType", awaitable.getClass().getSimpleName());
            if (awaitable instanceof TypeRequest<?> typeRequest) {
                result.put("requestedType", typeRequest.getType().getSimpleName());
                result.put("message", typeRequest.getMessage());
                result.put("hint", "POST /hitl-advanced/" + process.getId()
                        + "/account，body 为 RefundAccount 的 JSON");
            }
        } else if (process.getStatus() == AgentProcessStatusCode.COMPLETED) {
            result.put("output", process.lastResult());
        }
        return result;
    }
}
