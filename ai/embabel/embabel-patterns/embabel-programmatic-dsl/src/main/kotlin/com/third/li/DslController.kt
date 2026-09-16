package com.third.li

import com.embabel.agent.api.invocation.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 程序化 DSL 接口。
 */
@RestController
class DslController(private val agentPlatform: AgentPlatform) {

    /** 显式流水线：提纲 -> 初稿 -> 纯代码后处理 */
    @GetMapping("/dsl/pipeline")
    fun pipeline(
        @RequestParam(value = "topic", defaultValue = "为什么 Agent 需要类型化建模") topic: String,
    ): Article = AgentInvocation.create(agentPlatform, Article::class.java).invoke(ArticleRequest(topic))

    /** 扇出 + 汇总：多角度要点合并成简报 */
    @GetMapping("/dsl/digest")
    fun digest(
        @RequestParam(value = "topic", defaultValue = "在团队里引入 Agent 框架") topic: String,
    ): Digest = AgentInvocation.create(agentPlatform, Digest::class.java).invoke(ArticleRequest(topic))
}
