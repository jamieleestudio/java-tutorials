package com.third.li

import com.embabel.agent.api.common.InputActionContext
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.dsl.agent
import com.embabel.agent.api.dsl.aggregate
import com.embabel.agent.core.Agent
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.core.Condition
import com.embabel.plan.common.condition.ConditionDetermination
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 用 **Kotlin DSL** 以代码方式构建 Agent —— 等价于 `@Agent` / `@Action` / `@AchievesGoal`，
 * 但不依赖注解、完全类型安全，且可以在运行时动态拼装。
 *
 * 两个例子：
 * 1. `dslArticlePipeline`：`agent {}` + `promptedTransformer` + `transformation` + `goal`
 *    —— 与注解式写法一一对应的"显式流水线"。
 * 2. `dslAngleDigest`：`agent {}` + `flow { aggregate(...) }`
 *    —— 类型安全的**扇出 + 汇总**（scatter/gather），由 DSL 自动生成规划所需的条件。
 *
 * 要点：
 * - `promptedTransformer<I, O>` 会自动调用 LLM 并把结果反序列化成 `O`（结构化输出）。
 * - `transformation<I, O>` 是**纯代码**步骤，不调 LLM。
 * - 动作之间的顺序由**类型**推导（`UserInput -> Outline -> Draft -> Article`），
 *   因此不需要手写 pre/post 条件。
 * - `goal(satisfiedBy = X::class)` 声明目标类型，`AgentInvocation` 据此选择 Agent。
 */
@Configuration
class DslAgentConfig(private val agentPlatform: AgentPlatform) {

    /**
     * 程序化条件：主题非空。演示 DSL 的 `Condition` 能力（可用作 pre/post）。
     */
    private val topicNonEmpty: Condition = object : Condition {
        override val name = "topicNonEmpty"
        override val cost = 0.0
        override fun evaluate(context: OperationContext): ConditionDetermination =
            ConditionDetermination(!context.last(ArticleRequest::class.java)?.topic.isNullOrBlank())
    }

    @Bean
    fun dslArticlePipeline(): Agent = agent(
        name = "DslArticlePipeline",
        description = "程序化 DSL 构建的文章流水线：提纲 -> 初稿 -> 纯代码后处理",
    ) {
        // 注册条件，使规划器知道它的存在
        condition { topicNonEmpty }

        // 第 1 步：LLM 生成提纲（ArticleRequest -> Outline）
        promptedTransformer<ArticleRequest, Outline>(
            name = "makeOutline",
            description = "为主题生成 3 点提纲",
            pre = listOf(topicNonEmpty),
        ) { ctx ->
            "为主题「${ctx.input.topic}」生成 3 点提纲，每行一点，不要任何多余解释。"
        }

        // 第 2 步：LLM 写初稿（Outline -> Draft）
        promptedTransformer<Outline, Draft>(
            name = "writeDraft",
            description = "根据提纲写初稿",
        ) { ctx ->
            "根据下面的提纲写一篇 200 字以内的短文：\n${ctx.input.points.joinToString("\n")}"
        }

        // 第 3 步：纯代码后处理（Draft -> Article），不调 LLM
        transformation<Draft, Article>(
            name = "polish",
            description = "纯代码后处理：加标题前缀并统计字数",
        ) { ctx ->
            Article(
                title = "【DSL】${ctx.input.title}",
                body = ctx.input.body,
                wordCount = ctx.input.body.length,
            )
        }

        goal(
            name = "ProduceArticle",
            description = "产出一篇带统计信息的文章",
            satisfiedBy = Article::class,
        )
    }

    @Bean
    fun dslAngleDigest(): Agent = agent(
        name = "DslAngleDigest",
        description = "程序化 DSL 的 flow + aggregate：多角度扇出后汇总成简报",
    ) {
        flow {
            aggregate<ArticleRequest, AngleNote, Digest>(
                transforms = listOf(
                    { ctx -> note(ctx, "技术可行性") },
                    { ctx -> note(ctx, "产品价值") },
                    { ctx -> note(ctx, "风险与成本") },
                ),
                merge = { notes, ctx ->
                    val topic = ctx.last(ArticleRequest::class.java)?.topic ?: "未知主题"
                    Digest(
                        topic = topic,
                        summary = ctx.ai().withDefaultLlm().generateText(
                            """
                            主题：$topic
                            以下是并行产出的要点：
                            ${notes.joinToString("\n") { "- [${it.angle}] ${it.text}" }}

                            请综合这些要点，写一段 150 字以内的结论。
                            """.trimIndent()
                        ),
                        notes = notes,
                    )
                },
            )
        }

        goal(
            name = "ProduceDigest",
            description = "产出多角度简报",
            satisfiedBy = Digest::class,
        )
    }

    private fun note(ctx: InputActionContext<ArticleRequest>, angle: String): AngleNote {
        val topic = ctx.input.topic
        return AngleNote(
            angle = angle,
            text = ctx.ai().withDefaultLlm()
                .generateText("请从「$angle」角度，用一句话给出关于「$topic」的要点。"),
        )
    }

    /**
     * 把 DSL 构建出来的 Agent 注册到平台。
     * （`@Bean Agent` 的自动注册依赖扫描开关，这里显式 deploy，行为确定。）
     */
    @Bean
    fun deployDslAgents(agents: List<Agent>): ApplicationRunner =
        ApplicationRunner { agents.forEach { agentPlatform.deploy(it) } }
}
