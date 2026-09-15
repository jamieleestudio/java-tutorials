package com.third.li

/** 工作流的输入：主题。 */
data class Topic(val text: String)

/** 扇出阶段生成的单个要点。 */
data class Idea(val angle: String, val text: String)

/** 汇总后的文章。 */
data class Article(val content: String)

/** 共识工作流的输入：问题。 */
data class Question(val text: String)

/** 单个模型的回答。 */
data class Draft(val model: String, val text: String)

/** 共识后的最终回答。 */
data class Answer(val content: String, val sources: List<String>)
