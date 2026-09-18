package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Refinement 模式（自评迭代 / Evaluator-Optimizer）。
 *
 * <p>生成 → 评估 → 改进 的循环，直到评估分数达标或达到最大轮次。
 * 与 Embabel embabel-refinement / AgentScope agentscope-refinement 对照。
 */
@RestController
public class RefinementController {

    private final ChatClient chatClient;

    public RefinementController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/refinement")
    public String refine(
            @RequestParam(value = "message", defaultValue = "写一段关于 AI Agent 框架演进的技术文案") String message) {
        int maxRounds = 3;
        String current = message;
        StringBuilder log = new StringBuilder("【Refinement 自评迭代】\n");

        for (int round = 1; round <= maxRounds; round++) {
            // 生成
            String content = chatClient.prompt()
                    .system("你是内容生成器" + (round == 1 ? "" : "，根据改进建议优化产出。"))
                    .user(round == 1 ? current : "改进建议：\n" + current)
                    .call().content();

            // 评估
            String evalText = chatClient.prompt()
                    .system("你是评估器。对内容打分（1-10），格式：评分：N分。建议：...")
                    .user(content)
                    .call().content();

            double score = extractScore(evalText);
            log.append("第 ").append(round).append(" 轮评分：").append(score).append("\n");

            if (score >= 8.0) {
                return log.append("\n【达标】最终内容：\n").append(content).toString();
            }
            current = evalText;
        }

        String finalContent = chatClient.prompt()
                .system("你是内容生成器，根据改进建议优化产出。")
                .user("改进建议：\n" + current)
                .call().content();
        return log.append("\n【达到最大轮次】最终内容：\n").append(finalContent).toString();
    }

    private double extractScore(String text) {
        if (text == null) return 0;
        Matcher m = Pattern.compile("(\\d+)\\s*分").matcher(text);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
