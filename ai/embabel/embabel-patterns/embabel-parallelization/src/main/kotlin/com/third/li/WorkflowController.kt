package com.third.li

import com.embabel.agent.api.invocation.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 工作流原语接口（Kotlin builder 版本，与 Java 手写版本对照）。
 *
 * <p>路径统一挂在 `/parallel` 前缀下，和 {@link ParallelizationController} 的两个端点并列。
 */
@RestController
class WorkflowController(private val agentPlatform: AgentPlatform) {

    /** ScatterGather：并行扇出 + 汇总 */
    @GetMapping("/parallel/scatter-gather")
    fun scatterGather(
        @RequestParam(value = "topic", defaultValue = "为什么要用 Agent 框架而不是写死工作流") topic: String,
    ): Article = AgentInvocation.create(agentPlatform, Article::class.java).invoke(Topic(topic))

    /** Consensus：多模型共识 */
    @GetMapping("/parallel/consensus")
    fun consensus(
        @RequestParam(value = "question", defaultValue = "微服务架构适合什么规模的团队？") question: String,
    ): Answer = AgentInvocation.create(agentPlatform, Answer::class.java).invoke(Question(question))
}
