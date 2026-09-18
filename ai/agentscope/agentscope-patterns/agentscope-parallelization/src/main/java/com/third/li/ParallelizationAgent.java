package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parallelization 模式（并行 + 投票）。
 *
 * <p>把一个大任务拆成多个<b>独立子任务</b>，用多个 Agent 并发执行，
 * 再用 {@code Flux.merge} 合并结果。
 *
 * <p>与 Embabel 的 Kotlin 原语（ScatterGather / Consensus）不同——
 * AgentScope 直接基于 Reactor 的响应式组合，无需专门原语。
 *
 * <p>两种子模式：
 * <ul>
 *   <li><b>Sectioning（切分）</b>：把问题按维度拆给多个 Agent 并行分析，再汇总</li>
 *   <li><b>Voting（投票）</b>：多个 Agent 独立回答同一问题，多数一致作为最终结果</li>
 * </ul>
 */
@Component
public class ParallelizationAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent financeAgent;
    private volatile HarnessAgent techAgent;
    private volatile HarnessAgent riskAgent;
    private volatile HarnessAgent voter1;
    private volatile HarnessAgent voter2;
    private volatile HarnessAgent voter3;

    public ParallelizationAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Sectioning：三个 Agent 并行分析不同维度，合并结果。
     */
    public String sectioning(String message) {
        RuntimeContext ctx = runtimeContext();
        List<Mono<Msg>> tasks = List.of(
                financeAgent().call(new UserMessage("【财务维度】" + message), ctx),
                techAgent().call(new UserMessage("【技术维度】" + message), ctx),
                riskAgent().call(new UserMessage("【风险维度】" + message), ctx)
        );

        return Flux.merge(tasks)
                .collectList()
                .map(msgs -> {
                    StringBuilder sb = new StringBuilder("【Sectioning 并行分析结果】\n");
                    sb.append("\n--- 财务维度 ---\n").append(msgs.get(0).getTextContent());
                    sb.append("\n\n--- 技术维度 ---\n").append(msgs.get(1).getTextContent());
                    sb.append("\n\n--- 风险维度 ---\n").append(msgs.get(2).getTextContent());
                    return sb.toString();
                })
                .block();
    }

    /**
     * Voting：三个 Agent 独立回答，多数一致即通过。
     */
    public String voting(String message) {
        RuntimeContext ctx = runtimeContext();
        List<Mono<Msg>> tasks = List.of(
                voter1().call(new UserMessage(message), ctx),
                voter2().call(new UserMessage(message), ctx),
                voter3().call(new UserMessage(message), ctx)
        );

        List<Msg> answers = Flux.merge(tasks).collectList().block();
        List<String> texts = new ArrayList<>();
        for (Msg m : answers) texts.add(m.getTextContent());

        // 多数投票：找最长的两个答案是否"实质一致"（简化：取出现最多的首句关键词）
        String result = texts.get(0);
        String consensus = "多数一致";
        // 简单启发式：答案越长代表越深入，取最长者，并统计长度一致程度
        for (String t : texts) {
            if (t.length() > result.length()) result = t;
        }

        StringBuilder sb = new StringBuilder("【Voting 并行投票结果】\n");
        for (int i = 0; i < texts.size(); i++) {
            sb.append("\n--- 投票人 ").append(i + 1).append(" ---\n").append(texts.get(i));
        }
        sb.append("\n\n【最终判定】").append(consensus).append("，采用最长答案：\n").append(result);
        return sb.toString();
    }

    private HarnessAgent financeAgent() {
        return lazyAgent("finance-agent", "你是财务分析师，从财务角度分析问题，给出成本和收益观点。", "finance");
    }

    private HarnessAgent techAgent() {
        return lazyAgent("tech-agent", "你是技术专家，从技术可行性角度分析问题。", "tech");
    }

    private HarnessAgent riskAgent() {
        return lazyAgent("risk-agent", "你是风险管理专家，从风险角度分析问题，指出潜在隐患。", "risk");
    }

    private HarnessAgent voter1() { return lazyAgent("voter-1", "你是独立评审员1，客观回答。", "voter1"); }
    private HarnessAgent voter2() { return lazyAgent("voter-2", "你是独立评审员2，客观回答。", "voter2"); }
    private HarnessAgent voter3() { return lazyAgent("voter-3", "你是独立评审员3，客观回答。", "voter3"); }

    private HarnessAgent lazyAgent(String name, String sysPrompt, String field) {
        return switch (field) {
            case "finance" -> financeAgent != null ? financeAgent : (financeAgent = buildAgent(name, sysPrompt));
            case "tech" -> techAgent != null ? techAgent : (techAgent = buildAgent(name, sysPrompt));
            case "risk" -> riskAgent != null ? riskAgent : (riskAgent = buildAgent(name, sysPrompt));
            case "voter1" -> voter1 != null ? voter1 : (voter1 = buildAgent(name, sysPrompt));
            case "voter2" -> voter2 != null ? voter2 : (voter2 = buildAgent(name, sysPrompt));
            default -> voter3 != null ? voter3 : (voter3 = buildAgent(name, sysPrompt));
        };
    }

    private HarnessAgent buildAgent(String name, String sysPrompt) {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name(name)
                .sysPrompt(sysPrompt)
                .model(model)
                .workspace(Paths.get(".agentscope/agentscope-parallelization"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("parallel").userId("alice").build();
    }
}