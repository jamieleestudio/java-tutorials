package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Refinement 模式（Evaluator-Optimizer 自评迭代）。
 *
 * <p>生成 → 评估 → 改进 的迭代循环，直到评估通过或达到最大迭代次数。
 *
 * <p>与 Embabel 的 {@code embabel-refinement} 相同思想——Embabel 靠 goal 机制
 * 判断是否满足；AgentScope 用<b>评估 Agent</b> 显式打分。
 *
 * <p>本模块演示：
 * <ol>
 *   <li>生成器 Agent（generator）：产出内容</li>
 *   <li>评估器 Agent（evaluator）：结构化输出评分 + 改进建议</li>
 *   <li>循环：分数低于 7 分则带着建议重新生成，最多 3 轮</li>
 * </ol>
 */
@Component
public class RefinementAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent generator;
    private volatile HarnessAgent evaluator;

    public RefinementAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 自评迭代：生成 → 评估 → 改进，直到分数达标或达到最大轮次。
     */
    public String refine(String message) {
        RuntimeContext ctx = runtimeContext();
        int maxRounds = 3;
        String current = message;

        StringBuilder log = new StringBuilder("【Refinement 自评迭代】\n\n");

        for (int round = 1; round <= maxRounds; round++) {
            Msg generated = generator().call(new UserMessage(
                    round == 1 ? current : "请根据以下建议改进。\n建议：" + current), ctx).block();
            String content = generated.getTextContent();

            Msg evaluated = evaluator().call(new UserMessage(
                    "请评估以下内容，输出评分（1-10）和改进建议。\n\n" + content), ctx).block();

            String evalText = evaluated.getTextContent();
            double score = extractScore(evalText);
            log.append("第 ").append(round).append(" 轮：评分 ").append(score)
                    .append(" 分\n");

            if (score >= 7.0) {
                log.append("\n【达标！】最终内容（第 ").append(round).append(" 轮）：\n").append(content);
                return log.toString();
            }
            current = evalText; // 把评估建议作为下一轮的改进依据
        }

        Msg finalGen = generator().call(new UserMessage(
                "请根据以下建议改进。\n建议：" + current), ctx).block();
        log.append("\n【达到最大轮次，采用最终改进】\n").append(finalGen.getTextContent());
        return log.toString();
    }

    private double extractScore(String evalText) {
        if (evalText == null) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*[分/]").matcher(evalText);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        // 兜底：不含数字则视为不合格，触发改进
        return 0;
    }

    private HarnessAgent generator() {
        HarnessAgent local = generator;
        if (local == null) {
            synchronized (this) {
                local = generator;
                if (local == null) {
                    local = buildAgent("generator", "你是内容生成器。根据要求产出高质量内容，"
                            + "若给了改进建议则据此优化。");
                    generator = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent evaluator() {
        HarnessAgent local = evaluator;
        if (local == null) {
            synchronized (this) {
                local = evaluator;
                if (local == null) {
                    local = buildAgent("evaluator", "你是内容评估器。对内容打分（1-10），"
                            + "输出格式：'评分：7分。建议：...'。严格打分，低于 7 分说明需要改进。");
                    evaluator = local;
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
                .workspace(Paths.get(".agentscope/agentscope-refinement"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("refinement").userId("alice").build();
    }
}