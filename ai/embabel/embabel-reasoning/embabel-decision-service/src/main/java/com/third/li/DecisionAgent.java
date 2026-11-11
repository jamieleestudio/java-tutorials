package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.classification.ClassificationResult;
import com.embabel.common.ai.classification.ClassificationSpec;
import com.embabel.common.ai.decision.DecisionAnswer;
import com.embabel.common.ai.decision.DecisionResponse;
import com.embabel.common.ai.decision.DecisionSpec;
import com.embabel.common.ai.decision.PropositionResult;
import com.embabel.common.ai.decision.RatingResult;

/**
 * 类型安全决策服务（Embabel 1.5.4 新特性）。
 * <p>1.5.4 引入 {@code Ai.decisions()} / {@code Ai.classifications()} 两个 ServiceSelector：
 * 平台按配置自动注册 {@code LlmDecisionServiceFactory}（LLM 后端，DeepSeek 兼容端点即可），
 * 也可以换成 System One 协议的决策模型提供商 —— 调用代码完全不变。
 *
 * <p>与 {@code .creating(X.class).fromPrompt(...)}（让 LLM 生成整个对象）相比，
 * 决策服务的语义是**一次提问、类型化答案、带概率**：
 * <ul>
 *   <li>{@code ClassificationService.classify(text, spec)}：多分类，返回
 *       {@link ClassificationResult}（Selected 带 categoryId + confidence + 分布）</li>
 *   <li>{@code DecisionService.ask(text, spec)}：一次问一组问题 ——
 *       proposition（是/否）、choice（单选）、rating（评级），分别得到
 *       Proposition / Choice / Rating 三种答案</li>
 * </ul>
 */
@Agent(description = "决策服务：分类 + 组合决策（是/否 + 单选 + 评级）")
public class DecisionAgent {

    public record TicketReport(
            String category,
            boolean refundIntent,
            String priority,
            String urgency,
            String advice) {
    }

    @Action
    @AchievesGoal(description = "产出客理工单决策报告")
    public TicketReport decide(UserInput userInput, Ai ai) {
        String text = userInput.getContent();

        // 1. 多分类（ClassificationService）
        ClassificationResult classification = ai.classifications().defaultService().classify(
                text,
                ClassificationSpec.builder()
                        .asking("这条用户消息属于哪一类？")
                        .category("COMPLAINT", "投诉、强烈不满、要求赔偿处理")
                        .category("REFUND", "退款、退货请求")
                        .category("CONSULT", "一般咨询")
                        .category("OTHER", "其它")
                        .build());
        String category = classification instanceof ClassificationResult.Selected selected
                ? selected.getCategoryId()
                : "UNKNOWN";

        // 2. 组合决策（DecisionService）：一次调用问三个问题
        DecisionSpec spec = DecisionSpec.builder()
                .proposition("refundIntent", b -> b.asking("用户是否明确表达退款/退货意图？"))
                .choice("priority", b -> b.asking("处理优先级如何？")
                        .option("HIGH", "涉及资金损失、严重故障或强烈不满")
                        .option("NORMAL", "一般诉求，可正常排期"))
                .rating("urgency", b -> b.asking("紧急程度评级")
                        .level("P0", "立即处理")
                        .level("P1", "当天处理")
                        .level("P2", "可排期处理"))
                .build();
        DecisionResponse response = ai.decisions().defaultService().ask(text, spec);

        boolean refundIntent = false;
        String priority = "NORMAL";
        String urgency = "P2";
        double refundProbability = 0.0;
        for (DecisionAnswer answer : response.getAnswers()) {
            switch (answer) {
                case DecisionAnswer.Proposition p -> {
                    if (p.getOutcome() instanceof PropositionResult.Answered answered) {
                        refundIntent = answered.getAnswer();
                        refundProbability = answered.getPTrue() == null ? 0.0 : answered.getPTrue();
                    }
                }
                case DecisionAnswer.Choice c ->
                        priority = c.getOutcome() instanceof ClassificationResult.Selected selected
                                ? selected.getCategoryId()
                                : priority;
                case DecisionAnswer.Rating r -> {
                    if (r.getOutcome() instanceof RatingResult.Answered answered) {
                        urgency = answered.getSelectedLevelId();
                    }
                }
                default -> { /* 未支持的答案类型 */ }
            }
        }

        String advice = ai.withDefaultLlm().generateText("""
                你是客服主管。基于以下决策结果给出一句处理建议（不超过 40 字）：
                分类=%s，退款意图=%s（概率 %.2f），优先级=%s，紧急度=%s。
                用户消息：%s
                """.formatted(category, refundIntent, refundProbability, priority, urgency, text));

        return new TicketReport(category, refundIntent, priority, urgency, advice);
    }
}
