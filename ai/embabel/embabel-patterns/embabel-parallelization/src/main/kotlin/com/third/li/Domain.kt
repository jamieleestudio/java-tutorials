package com.third.li

/**
 * 工作流原语示例（原 `embabel-workflows` 模块）用到的领域类型。
 *
 * 注意：这些类型与同模块 Java 侧的 `Report` / `Verdict` 等**互不冲突**，
 * 而且四个 Agent 的目标类型各不相同（`Report` / `Verdict` / `Article` / `Answer`），
 * 这样 `AgentInvocation.create(platform, X.class)` 才能按目标类型唯一选中 Agent。
 */

/** ScatterGather 的输入：主题。 */
data class Topic(val text: String)

/** 扇出阶段生成的单个要点。 */
data class Idea(val angle: String, val text: String)

/** ScatterGather 汇总后的文章（目标类型）。 */
data class Article(val content: String)

/** Consensus 的输入：问题。 */
data class Question(val text: String)

/** 单个来源（模型角色）的回答。 */
data class Draft(val model: String, val text: String)

/** Consensus 的最终回答（目标类型）。 */
data class Answer(val content: String, val sources: List<String>)
