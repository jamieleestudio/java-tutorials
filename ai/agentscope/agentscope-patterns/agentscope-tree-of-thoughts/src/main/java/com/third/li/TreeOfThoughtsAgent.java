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
import java.util.ArrayList;
import java.util.List;

/**
 * Tree of Thoughts 模式（思维树）。
 *
 * <p>同时探索<b>多条思维分支</b>，评分后剪枝，取最优分支继续深化——
 * 与 Embabel 的 {@code embabel-tree-of-thoughts}（分支生成 + 评分 + 剪枝 + 取最优）同思路。
 *
 * <p>本模块演示两层思维树：
 * <ol>
 *   <li><b>分支生成</b>：3 个 Agent 并行产出不同解决思路</li>
 *   <li><b>评分剪枝</b>：评估 Agent 对每个思路打分，淘汰低分</li>
 *   <li><b>深化</b>：对最优思路生成详细方案</li>
 * </ol>
 */
@Component
public class TreeOfThoughtsAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent thinker1;
    private volatile HarnessAgent thinker2;
    private volatile HarnessAgent thinker3;
    private volatile HarnessAgent scorer;
    private volatile HarnessAgent elaborator;

    public TreeOfThoughtsAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 思维树：分支生成 → 评分剪枝 → 最优深化。
     */
    public String solve(String problem) {
        RuntimeContext ctx = runtimeContext();
        List<Msg> thoughts = Flux.merge(
                thinker1().call(new UserMessage("思路1：" + problem), ctx),
                thinker2().call(new UserMessage("思路2：" + problem), ctx),
                thinker3().call(new UserMessage("思路3：" + problem), ctx)
        ).collectList().block();

        // 评分：选出最佳思路
        StringBuilder scored = new StringBuilder("请为以下 3 个解决思路打分（1-10）并给出选择建议。\n");
        for (int i = 0; i < thoughts.size(); i++) {
            scored.append("\n思路").append(i + 1).append("：\n").append(thoughts.get(i).getTextContent());
        }
        Msg scoreResult = scorer().call(new UserMessage(scored.toString()), ctx).block();

        // 深化：取评估推荐的最优思路深化
        Msg detail = elaborator().call(new UserMessage(
                "请把得分最高的思路展开成完整的实施方案。\n\n" + thoughts.get(0).getTextContent()
                        + "\n\n评估结果：\n" + scoreResult.getTextContent()), ctx).block();

        return "【思维树探索】\n"
                + "\n===== 分支 1 =====\n" + thoughts.get(0).getTextContent()
                + "\n\n===== 分支 2 =====\n" + thoughts.get(1).getTextContent()
                + "\n\n===== 分支 3 =====\n" + thoughts.get(2).getTextContent()
                + "\n\n===== 评分与选择 =====\n" + scoreResult.getTextContent()
                + "\n\n===== 最优方案深化 =====\n" + detail.getTextContent();
    }

    private HarnessAgent thinker1() { return lazy("thinker-1", "你是发散思维专家，从技术角度给出思路。", 1); }
    private HarnessAgent thinker2() { return lazy("thinker-2", "你是发散思维专家，从业务角度给出思路。", 2); }
    private HarnessAgent thinker3() { return lazy("thinker-3", "你是发散思维专家，从用户角度给出思路。", 3); }

    private HarnessAgent scorer() {
        HarnessAgent local = scorer;
        if (local == null) {
            synchronized (this) {
                local = scorer;
                if (local == null) {
                    local = buildAgent("scorer", "你是评估专家。给每个思路打分（1-10），" +
                            "并明确推荐得分最高的思路。输出格式：思路X：Y分。推荐：思路X");
                    scorer = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent elaborator() {
        HarnessAgent local = elaborator;
        if (local == null) {
            synchronized (this) {
                local = elaborator;
                if (local == null) {
                    local = buildAgent("elaborator", "你是方案专家。把思路展开成详细、可执行的完整方案。");
                    elaborator = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent lazy(String name, String sysPrompt, int n) {
        return switch (n) {
            case 1 -> thinker1 != null ? thinker1 : (thinker1 = buildAgent(name, sysPrompt));
            case 2 -> thinker2 != null ? thinker2 : (thinker2 = buildAgent(name, sysPrompt));
            default -> thinker3 != null ? thinker3 : (thinker3 = buildAgent(name, sysPrompt));
        };
    }

    private HarnessAgent buildAgent(String name, String sysPrompt) {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name(name)
                .sysPrompt(sysPrompt)
                .model(model)
                .workspace(Paths.get(".agentscope/agentscope-tree-of-thoughts"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("tree-of-thoughts").userId("alice").build();
    }
}