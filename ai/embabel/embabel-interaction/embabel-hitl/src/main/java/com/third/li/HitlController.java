package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.hitl.Awaitable;
import com.embabel.agent.core.hitl.ConfirmationRequest;
import com.embabel.agent.core.hitl.FormBindingRequest;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 人机协同接口。
 *
 * <p>流程：先启动流程，若返回 {@code WAITING} 则把 {@code processId} 和待确认内容
 * 返回给前端；前端再调用恢复接口（确认 / 提交表单）让流程继续。
 */
@RestController
@RequestMapping("/hitl")
public class HitlController {

    private final AgentPlatform agentPlatform;

    public HitlController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** 启动"生成草稿 -> 人工确认"流程 */
    @GetMapping("/review")
    public Map<String, Object> review(
            @RequestParam(value = "message", defaultValue = "为一款智能咖啡机写一句广告语") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, ChatReply.class)
                .run(new UserInput(message));
        return describe(process);
    }

    /** 对等待中的确认请求作出响应，并恢复流程 */
    @PostMapping("/{processId}/confirm")
    public Map<String, Object> confirm(
            @PathVariable String processId,
            @RequestParam boolean accepted) {
        AgentProcess process = agentPlatform.getAgentProcess(processId);
        if (process == null) {
            return Map.of("status", "NOT_FOUND", "processId", processId);
        }
        if (!accepted) {
            return Map.of("status", "REJECTED", "processId", processId, "message", "已拒绝，草稿未发布");
        }
        Awaitable<?, ?> awaitable = (Awaitable<?, ?>) process.lastResult();
        process.addObject(awaitable.getPayload());
        process.run();
        return describe(process);
    }

    /** 启动"表单收集信息"流程 */
    @GetMapping("/contact")
    public Map<String, Object> contact(
            @RequestParam(value = "message", defaultValue = "帮我生成一封跟进邮件") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, ContactReply.class)
                .run(new UserInput(message));
        return describe(process);
    }

    /** 提交表单，恢复流程 */
    @PostMapping("/{processId}/form")
    @SuppressWarnings("unchecked")
    public Map<String, Object> submitForm(
            @PathVariable String processId,
            @RequestBody ContactInfo contact) {
        AgentProcess process = agentPlatform.getAgentProcess(processId);
        if (process == null) {
            return Map.of("status", "NOT_FOUND", "processId", processId);
        }
        Awaitable<?, ?> awaitable = (Awaitable<?, ?>) process.lastResult();
        if (awaitable instanceof FormBindingRequest<?> formBindingRequest) {
            ((FormBindingRequest<ContactInfo>) formBindingRequest).bind(contact, process);
            process.run();
        }
        return describe(process);
    }

    private Map<String, Object> describe(AgentProcess process) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processId", process.getId());
        result.put("status", process.getStatus().name());
        if (process.getStatus() == AgentProcessStatusCode.WAITING) {
            Awaitable<?, ?> awaitable = (Awaitable<?, ?>) process.lastResult();
            result.put("awaitableType", awaitable.getClass().getSimpleName());
            if (awaitable instanceof ConfirmationRequest<?> confirmationRequest) {
                result.put("question", confirmationRequest.getMessage());
                result.put("payload", confirmationRequest.getPayload());
                result.put("hint", "POST /hitl/" + process.getId() + "/confirm?accepted=true|false");
            } else if (awaitable instanceof FormBindingRequest<?> formBindingRequest) {
                result.put("form", formBindingRequest.getPayload().toString());
                result.put("outputClass", formBindingRequest.getOutputClass().getSimpleName());
                result.put("hint", "POST /hitl/" + process.getId() + "/form，body 为 ContactInfo 的 JSON");
            }
        } else if (process.getStatus() == AgentProcessStatusCode.COMPLETED) {
            result.put("output", process.lastResult());
        }
        return result;
    }
}
