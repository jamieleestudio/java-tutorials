package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.util.List;

/**
 * Debate 模式（多 Agent 辩论）。
 *
 * <p>两个 Agent 持<b>对立立场</b>就同一议题辩论，第三个 Agent 当<b>裁判</b>
 * 综合双方观点给出最终结论。
 *
 * <p>与 Embabel 的 {@code embabel-debate} 对照——Embabel 用对立视角 + 裁判综合；
 * AgentScope 用<b>并发</b>（{@code Flux.merge}）让正反方同时发言，再串行裁判。
 *
 * <p>本模块演示：
 * <ul>
 *   <li>正方 Agent（pro）：支持议题</li>
 *   <li>反方 Agent（con）：反对议题</li>
 *   <li>裁判 Agent（judge）：听取双方后裁定</li>
 * </ul>
 */
@Component
public class DebateAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent proAgent;
    private volatile HarnessAgent conAgent;
    private volatile HarnessAgent judgeAgent;

    public DebateAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 辩论：正反方并发发言 → 裁判综合。
     */
    public String debate(String topic) {
        RuntimeContext ctx = runtimeContext();
        List<Msg> sides = Flux.merge(
                proAgent().call(new UserMessage("议题：" + topic + "\n请给出支持该议题的论点（3 条）"), ctx),
                conAgent().call(new UserMessage("议题：" + topic + "\n请给出反对该议题的论点（3 条）"), ctx)
        ).collectList().block();

        String pro = sides.get(0).getTextContent();
        String con = sides.get(1).getTextContent();

        Msg verdict = judgeAgent().call(new UserMessage(
                "议题：" + topic + "\n\n正方观点：\n" + pro + "\n\n反方观点：\n" + con
                        + "\n\n请作为中立裁判，综合双方论点，给出最终裁定和理由。"), ctx).block();

        return "【辩论：】" + topic
                + "\n\n===== 正方 =====\n" + pro
                + "\n\n===== 反方 =====\n" + con
                + "\n\n===== 裁判裁定 =====\n" + verdict.getTextContent();
    }

    private HarnessAgent proAgent() {
        HarnessAgent local = proAgent;
        if (local == null) {
            synchronized (this) {
                local = proAgent;
                if (local == null) {
                    local = buildAgent("pro", "你是正方辩手，坚定支持该议题，给出有说服力的论点。");
                    proAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent conAgent() {
        HarnessAgent local = conAgent;
        if (local == null) {
            synchronized (this) {
                local = conAgent;
                if (local == null) {
                    local = buildAgent("con", "你是反方辩手，坚定反对该议题，给出有力的反驳论点。");
                    conAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent judgeAgent() {
        HarnessAgent local = judgeAgent;
        if (local == null) {
            synchronized (this) {
                local = judgeAgent;
                if (local == null) {
                    local = buildAgent("judge", "你是中立裁判，客观评估正反双方论点，给出公平的最终裁定。");
                    judgeAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent buildAgent(String name, String sysPrompt) {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name(name)
                .sysPrompt(sysPrompt)
                .model(model)
                .workspace(Paths.get(".agentscope/agentscope-debate"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("debate").userId("alice").build();
    }
}