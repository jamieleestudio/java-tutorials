package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.event.UserConfirmResultEvent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.HookEventType;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * 人工确认（HITL）Agent：配置 ASK 权限规则，当模型调用危险工具时
 * 触发 {@link RequireUserConfirmEvent}，等待用户确认后再执行。
 *
 * <p>AgentScope 的 HITL 流程：
 * <ol>
 *   <li>权限引擎判定工具调用为 ASK → 发出 {@link RequireUserConfirmEvent}</li>
 *   <li>用户通过 {@link UserConfirmResultEvent} 回复确认/拒绝（{@link ConfirmResult}）</li>
 *   <li>确认则继续执行，拒绝则跳过该工具调用</li>
 * </ol>
 *
 * <p>本例用一个 {@link ConfirmLoggingHook} 监听确认事件并记录日志，
 * 演示如何接入人工确认流程（生产环境可接 Webhook / IM 审批）。
 */
@Component
public class HitlConfirmAgent {

    private static final Logger log = LoggerFactory.getLogger(HitlConfirmAgent.class);
    private static final String WORKSPACE_DIR = ".agentscope/workspace-hitl-confirm";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public HitlConfirmAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    PermissionRule askRule = new PermissionRule(
                            "*", "任何工具调用都需用户确认", PermissionBehavior.ASK, "hitl-demo");
                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .addAskRule("*", askRule)
                            .build();

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("hitl-confirm")
                            .sysPrompt("你是一个智能助手。每次调用工具前会请求用户确认。")
                            .model(model)
                            .permissionContext(permCtx)
                            .hook(new ConfirmLoggingHook())
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("hitl-demo").userId("alice").build();
    }

    /**
     * 监听确认事件的 Hook：记录 {@link RequireUserConfirmEvent} 和
     * {@link UserConfirmResultEvent}，演示 HITL 接入点。
     */
    static class ConfirmLoggingHook implements Hook {

        @Override
        public <T extends HookEvent> reactor.core.publisher.Mono<T> onEvent(T event) {
            return reactor.core.publisher.Mono.just(event);
        }

        @Override
        public int priority() {
            return 50;
        }
    }

    /** 记录确认相关事件的辅助方法，可在事件流订阅时调用。 */
    public static void logConfirmEvent(AgentEvent e) {
        if (e instanceof RequireUserConfirmEvent req) {
            log.info("[hitl] 需要确认：replyId={}，工具调用数={}", req.getReplyId(), req.getToolCalls().size());
        } else if (e instanceof UserConfirmResultEvent res) {
            List<ConfirmResult> results = res.getConfirmResults();
            log.info("[hitl] 用户确认结果：{}", results.stream()
                    .map(r -> (r.isConfirmed() ? "确认" : "拒绝") + "(" + r.getToolCall().getName() + ")")
                    .reduce("", (a, b) -> a + b + " "));
        }
    }
}