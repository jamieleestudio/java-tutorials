package com.third.li

import com.embabel.agent.api.common.SupplierActionContext
import com.embabel.agent.api.common.workflow.control.ScatterGatherBuilder
import com.embabel.agent.api.common.workflow.multimodel.ConsensusBuilder
import com.embabel.agent.core.Agent
import com.embabel.agent.core.AgentPlatform
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

/**
 * 用 Embabel 的**工作流原语**（Kotlin builder）构建 Agent——与 Java 侧"手写 `@Action`"对照。
 *
 * <p>同一个并行化模式有两种写法：
 * <ul>
 *   <li><b>手写</b>（见 {@link SectioningAgent} / {@link VotingAgent}）：多个 `@Action` + 一个汇总
 *       `@Action`，靠类型依赖让规划器并发调度（需要 `process-type: CONCURRENT`）。</li>
 *   <li><b>原语</b>（本类）：`ScatterGatherBuilder` / `ConsensusBuilder` 自动生成
 *       "N 个生成动作 + 汇总动作 + 目标"的完整规划，无需关心并发配置与依赖连线。</li>
 * </ul>
 *
 * <p>生成器接收的是 {@link SupplierActionContext}，它本身就是 `ActionContext`，
 * 所以可以拿到 `ai()` 发起 LLM 调用。
 */
@Configuration
class WorkflowConfig(private val agentPlatform: AgentPlatform) {

    /**
     * ScatterGather：同一任务按多个角度**并行**扇出，再汇总成一个结论。
     * 等价于 Java 侧的 Sectioning，但并发度由 builder 管理。
     */
    @Bean
    fun scatterGatherAgent(): Agent =
        ScatterGatherBuilder.returning(Article::class.java)
            .fromElements(Idea::class.java)
            .withGenerators(
                listOf(
                    Function { ctx: SupplierActionContext<Idea> -> idea(ctx, "技术可行性") },
                    Function { ctx: SupplierActionContext<Idea> -> idea(ctx, "产品价值") },
                    Function { ctx: SupplierActionContext<Idea> -> idea(ctx, "风险与成本") },
                )
            )
            .consolidatedBy { ctx ->
                val ideas = ctx.input.results
                val topic = ctx.last(Topic::class.java)?.text ?: "未知主题"
                Article(
                    ctx.ai().withDefaultLlm().generateText(
                        """
                        主题：$topic
                        以下是并行产出的要点：
                        ${ideas.joinToString("\n") { "- [${it.angle}] ${it.text}" }}

                        请综合这些要点，写一段 200 字以内的结论。
                        """.trimIndent()
                    )
                )
            }
            .buildAgent("scatter-gather", "并行生成多个角度的要点并汇总成结论")

    private fun idea(ctx: SupplierActionContext<Idea>, angle: String): Idea {
        val topic = ctx.last(Topic::class.java)?.text ?: "未知主题"
        return Idea(
            angle = angle,
            text = ctx.ai().withDefaultLlm()
                .generateText("请从「$angle」角度，用一句话给出关于「$topic」的要点。")
        )
    }

    /**
     * Consensus：多个来源（这里是两个**模型角色**）各自给出答案，再达成共识。
     * 角色到模型的映射见 `application.yml` 的 `embabel.models.llms`。
     */
    @Bean
    fun consensusAgent(): Agent =
        ConsensusBuilder.returning(Answer::class.java)
            .withSources(
                listOf(
                    Function { ctx: SupplierActionContext<Answer> -> draft(ctx, "fast") },
                    Function { ctx: SupplierActionContext<Answer> -> draft(ctx, "deep") },
                )
            )
            .withConsensusBy { ctx ->
                val drafts = ctx.input.results
                val question = ctx.last(Question::class.java)?.text ?: "未知问题"
                Answer(
                    content = ctx.ai().withDefaultLlm().generateText(
                        """
                        问题：$question
                        两个模型的回答：
                        ${drafts.joinToString("\n") { "- (${it.sources.joinToString("/")}) ${it.content}" }}

                        请综合两者，给出一个更可靠的最终回答（200 字以内）。
                        """.trimIndent()
                    ),
                    sources = drafts.flatMap { it.sources }
                )
            }
            .buildAgent("consensus", "多个模型各自回答后达成共识")

    private fun draft(ctx: SupplierActionContext<Answer>, role: String): Answer {
        val question = ctx.last(Question::class.java)?.text ?: "未知问题"
        return Answer(
            content = ctx.ai().withLlmByRole(role).generateText("请简洁准确地回答：$question"),
            sources = listOf(role)
        )
    }

    /**
     * 把上面构建出来的 Agent 注册到平台。
     * （框架的 `@Bean Agent` 自动注册依赖扫描开关，这里显式 deploy，行为确定。）
     */
    @Bean
    fun deployWorkflowAgents(agents: List<Agent>): ApplicationRunner =
        ApplicationRunner { agents.forEach { agentPlatform.deploy(it) } }
}
