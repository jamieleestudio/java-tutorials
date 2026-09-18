package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prompt Chaining 模式（提示链 + 关卡）。
 *
 * <p>把复杂任务拆成多个<b>顺序步骤</b>，每步的输出作为下一步输入。
 * 与 Embabel 的 @Action 链、AgentScope 的多次 agent.call() 对照——
 * Spring AI 用多个 ChatClient 调用 + 代码层 gate 短路。
 *
 * <p>三步链：
 * <ol>
 *   <li><b>提炼</b>：压缩用户输入为核心需求</li>
 *   <li><b>分析</b>：深度分析（含关卡：结果过短则短路）</li>
 *   <li><b>总结</b>：整理成结构化结论</li>
 * </ol>
 */
@RestController
public class PromptChainingController {

    private final ChatClient chatClient;

    public PromptChainingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/prompt-chain")
    public String chain(
            @RequestParam(value = "message", defaultValue = "分析远程办公对团队效率的影响") String message) {
        // 1. 提炼
        String extracted = chatClient.prompt()
                .system("你是需求提炼器。把输入压缩成 1-2 句核心需求，只输出提炼结果。")
                .user(message)
                .call().content();

        // 2. 分析
        String analyzed = chatClient.prompt()
                .system("你是深度分析器。给出至少 3 个维度的深入分析，越详细越好。")
                .user(extracted)
                .call().content();

        // 关卡（gate）：分析结果太短视为质量不过关
        if (analyzed == null || analyzed.length() < 50) {
            return "【关卡拦截】分析结果质量不足（" + (analyzed == null ? 0 : analyzed.length())
                    + " 字符 < 50），链条终止。\n\n分析输出：" + analyzed;
        }

        // 3. 总结
        String summary = chatClient.prompt()
                .system("你是总结器。把分析整理成「要点 + 建议」，200 字以内。")
                .user(analyzed)
                .call().content();

        return "【三步链完成】\n\n=== 1. 提炼 ===\n" + extracted
                + "\n\n=== 2. 分析 ===\n" + analyzed
                + "\n\n=== 3. 总结 ===\n" + summary;
    }
}
