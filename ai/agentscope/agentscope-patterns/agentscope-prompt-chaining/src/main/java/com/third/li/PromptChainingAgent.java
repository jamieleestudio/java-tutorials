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
 * Prompt Chaining 模式（提示链 + 关卡）。
 *
 * <p>把一个复杂任务拆成多个<b>顺序步骤</b>，每步的输出作为下一步的输入。
 * 与 Embabel 的 {@code @Action} 链不同——Embabel 靠规划器推导动作序列，
 * 而本模式在<b>代码层</b>显式编排多次 {@code agent.call()}。
 *
 * <p>本模块演示 3 步链：
 * <ol>
 *   <li><b>提炼</b>（extract）：从用户输入中提取关键需求</li>
 *   <li><b>分析</b>（analyze）：基于提炼结果做分析（含关卡：结果过短则短路）</li>
 *   <li><b>总结</b>（summarize）：把分析结果转成结构化总结</li>
 * </ol>
 *
 * <p><b>关卡（gate）</b>：第 2 步输出短于 20 字符视为"质量不过关"，
 * 直接终止链条，不进入第 3 步——这正是 Embabel gate 概念在代码层的等价物。
 */
@Component
public class PromptChainingAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent extractor;
    private volatile HarnessAgent analyzer;
    private volatile HarnessAgent summarizer;

    public PromptChainingAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 执行三步提示链。
     *
     * @return 链式结果（若关卡拦截则返回分析结果 + 提示）
     */
    public String chain(String message) {
        RuntimeContext ctx = runtimeContext();
        Msg extracted = extractor().call(new UserMessage(message), ctx).block();
        Msg analyzed = analyzer().call(new UserMessage(extracted.getTextContent()), ctx).block();

        // 关卡（gate）：分析结果太短视为质量不过关，链条终止
        String analyzedText = analyzed.getTextContent();
        if (analyzedText == null || analyzedText.length() < 20) {
            return "【关卡拦截】分析结果质量不足（" + (analyzedText == null ? 0 : analyzedText.length())
                    + " 字符 < 20），链条终止于第 2 步。\n\n分析输出：" + analyzedText;
        }

        Msg summarized = summarizer().call(new UserMessage(analyzedText), ctx).block();
        return "【三步链完成】\n\n=== 1. 提炼 ===\n" + extracted.getTextContent()
                + "\n\n=== 2. 分析 ===\n" + analyzedText
                + "\n\n=== 3. 总结 ===\n" + summarized.getTextContent();
    }

    private HarnessAgent extractor() {
        HarnessAgent local = extractor;
        if (local == null) {
            synchronized (this) {
                local = extractor;
                if (local == null) {
                    local = buildAgent("extractor", "你是一个需求提炼器。把用户输入压缩成 1-2 句核心需求，"
                            + "去掉冗余。只输出提炼后的需求，不要解释。");
                    extractor = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent analyzer() {
        HarnessAgent local = analyzer;
        if (local == null) {
            synchronized (this) {
                local = analyzer;
                if (local == null) {
                    local = buildAgent("analyzer", "你是一个深度分析器。基于提炼后的需求，"
                            + "给出至少 3 个维度的深入分析（原因、影响、对策），越详细越好。");
                    analyzer = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent summarizer() {
        HarnessAgent local = summarizer;
        if (local == null) {
            synchronized (this) {
                local = summarizer;
                if (local == null) {
                    local = buildAgent("summarizer", "你是一个总结器。把分析结果整理成"
                            + "「要点 + 建议」结构化的最终总结，控制 200 字以内。");
                    summarizer = local;
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
                .workspace(Paths.get(".agentscope/agentscope-prompt-chaining"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("prompt-chaining").userId("alice").build();
    }
}