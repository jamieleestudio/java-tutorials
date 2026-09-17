package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 实时打断（Agent.interrupt + stopOnReject）。
 *
 * <p>AgentScope 支持<b>实时打断</b>正在运行的 Agent：
 * <ul>
 *   <li>{@link HarnessAgent#interrupt()} — 中止当前执行，立即返回</li>
 *   <li>{@link HarnessAgent#interrupt(io.agentscope.core.message.Msg)} — 中止并注入一条消息</li>
 * </ul>
 *
 * <p>打断后，Agent 的 Flux 会 complete（不抛异常）。
 * AgentState 中 {@code shutdownInterrupted} 标记为 true。
 *
 * <p>本模块演示：
 * <ol>
 *   <li>启动一个长对话（异步）</li>
 *   <li>调用 {@code /interrupt/stop} 中止</li>
 *   <li>查看中止状态</li>
 * </ol>
 */
@Component
public class InterruptAgent {

    private static final Logger log = LoggerFactory.getLogger(InterruptAgent.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile String lastResult = "(未执行)";

    public InterruptAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /** 异步开始对话。 */
    public String startChat(String message) {
        if (!running.compareAndSet(false, true)) {
            return "已有对话在行中";
        }
        lastResult = "(运行中...)";
        agent().call(new UserMessage(message), runtimeContext())
                .doOnSuccess(r -> {
                    lastResult = "完成：" + r.getTextContent();
                    running.set(false);
                    log.info("[interrupt] 对话正常完成");
                })
                .doOnError(e -> {
                    lastResult = "出错：" + e.getMessage();
                    running.set(false);
                    log.warn("[interrupt] 对话出错：{}", e.getMessage());
                })
                .doOnCancel(() -> {
                    lastResult = "已取消";
                    running.set(false);
                    log.info("[interrupt] 对话被取消");
                })
                .subscribe();
        return "对话已启动，调用 /interrupt/stop 可中止";
    }

    /** 打断当前对话。 */
    public String interrupt() {
        if (!running.get()) {
            return "没有正在运行的对话";
        }
        agent().interrupt();
        running.set(false);
        return "已发送打断信号";
    }

    /** 查看状态。 */
    public String status() {
        return running.get()
                ? "状态：运行中。最后结果：" + lastResult
                : "状态：空闲。最后结果：" + lastResult;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("interrupt")
                            .sysPrompt("你是一个乐于助人的助手。请尽量详细回答用户的问题。")
                            .model(model)
                            .maxIters(10)
                            .workspace(Paths.get(".agentscope/workspace-interrupt"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("interrupt-demo").userId("alice").build();
    }
}