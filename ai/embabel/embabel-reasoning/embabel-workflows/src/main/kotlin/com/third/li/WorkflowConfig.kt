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
 * 用 Embabel 的**工作流原语**构建 Agent（Kotlin DSL 风格）。
 *
 * <p>两个例子：
 * <ul>
 *   <li><b>ScatterGather</b>：把同一任务按多个角度**并行**扇出，再把结果**汇总**成一个结论</li>
 *   <li><b>Consensus</b>：多个来源（这里是两个模型角色）各自给出答案，再**达成共识**</li>
 * </ul>
 *
 * <p>这两个 builder 内部会生成"生成动作 + 汇总动作 + 目标"的完整规划，
 * 因此可以用 {@code buildAgent(...)} 直接得到可注册的 Agent。
 * 生成器接收的是 {@code SupplierActionContext}，它本身就是 {@code ActionContext}，
 * 所以可以拿到 {@code ai()} 发起 LLM 调用。
 */
@Configuration
class WorkflowConfig(private val agentPlatform: AgentPlatform) {

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
     * （框架的 @Bean Agent 自动注册依赖扫描开关，这里显式 deploy，行为确定。）
     */
    @Bean
    fun deployWorkflowAgents(agents: List<Agent>): ApplicationRunner =
        ApplicationRunner { agents.forEach { agentPlatform.deploy(it) } }
}
