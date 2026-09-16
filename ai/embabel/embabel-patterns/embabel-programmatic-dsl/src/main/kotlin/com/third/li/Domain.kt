package com.third.li

/** 流水线输入：要写文章的主题。 */
data class ArticleRequest(val topic: String)

/** 第一步的产物：提纲。 */
data class Outline(val points: List<String>)

/** 第二步的产物：初稿。 */
data class Draft(val title: String, val body: String)

/** 最终产物（目标类型）：带统计信息的文章。 */
data class Article(val title: String, val body: String, val wordCount: Int)

/** 扇出阶段：某一个角度的要点。 */
data class AngleNote(val angle: String, val text: String)

/** 扇出汇总后的简报（目标类型）。 */
data class Digest(val topic: String, val summary: String, val notes: List<AngleNote>)
