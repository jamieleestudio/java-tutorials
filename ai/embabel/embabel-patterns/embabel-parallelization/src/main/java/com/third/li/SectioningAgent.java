package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * Parallelization - **Sectioning**（分片）：把任务拆成**互不依赖**的子任务并行处理，再汇总。
 *
 * <p>对应 Anthropic《Building Effective Agents》里 Parallelization 的第一个变体。
 * 典型用法就是文章举的例子：**一次调用干正事、另一次调用做检查**（guardrail），
 * 或对同一份内容从不同维度分别评审——每个 LLM 调用只需专注一个方面，效果更好。
 *
 * <p>这里的实现：三个维度（安全/性能/可维护性）各自是一个独立动作（都只依赖 {@link UserInput}），
 * 汇总动作同时需要三份结果。配置了
 * {@code embabel.agent.platform.process-type: CONCURRENT}，独立动作可并发执行。
 *
 * <p>对比（同一个模块里的另一种写法）：{@code WorkflowConfig} 用 Kotlin 原语
 * `ScatterGatherBuilder` 表达同一模式——它自动生成"N 个生成动作 + 汇总动作 + 目标"的规划，
 * 并发度由 builder 管理，无需手写 `@Action`、也不依赖 `process-type` 配置。
 * 端点：`/parallel/scatter-gather`。
 */
@Agent(description = "Sectioning：多维度并行评审后汇总")
public class SectioningAgent {

    @Action(description = "安全性评审")
    public SecurityReview security(UserInput userInput, Ai ai) {
        return new SecurityReview(ai.withDefaultLlm().withId("section-security").generateText(
                "请从安全性角度评审下面的内容，列出主要风险与建议：\n" + userInput.getContent()));
    }

    @Action(description = "性能评审")
    public PerformanceReview performance(UserInput userInput, Ai ai) {
        return new PerformanceReview(ai.withDefaultLlm().withId("section-performance").generateText(
                "请从性能角度评审下面的内容，列出瓶颈与优化建议：\n" + userInput.getContent()));
    }

    @Action(description = "可维护性评审")
    public MaintainabilityReview maintainability(UserInput userInput, Ai ai) {
        return new MaintainabilityReview(ai.withDefaultLlm().withId("section-maintainability").generateText(
                "请从可维护性角度评审下面的内容，列出问题与改进建议：\n" + userInput.getContent()));
    }

    @Action(description = "汇总三个维度的评审")
    @AchievesGoal(description = "产出评审报告")
    public Report report(
            SecurityReview security,
            PerformanceReview performance,
            MaintainabilityReview maintainability,
            UserInput userInput,
            Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("section-report")
                .generateText("""
                        请把三个维度的评审汇总成一份简明报告（含结论与优先级建议）：

                        【安全性】%s
                        【性能】%s
                        【可维护性】%s
                        """.formatted(security.content(), performance.content(), maintainability.content()));
        return new Report(content, 3);
    }
}
