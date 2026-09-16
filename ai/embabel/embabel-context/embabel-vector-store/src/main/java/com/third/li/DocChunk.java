package com.third.li;

import java.util.List;

/**
 * 知识库文档（内置语料）。
 *
 * <p>{@code source} 是**元数据**，用来演示"向量检索 + SQL 过滤"——
 * 这是内存里做余弦相似度做不到的（见 {@link PgVectorStore#search}）。
 */
public record DocChunk(
        String id,
        String title,
        String content,
        String source) {

    /** 内置语料：8 条，分属两个来源，便于演示元数据过滤。 */
    public static final List<DocChunk> DOCUMENTS = List.of(
            new DocChunk("d1", "什么是 Agent",
                    "Agent 是能自主决策并调用工具完成多步任务的系统。与固定工作流不同，"
                            + "Agent 的下一步由模型根据当前状态决定，因此能处理未预见的路径。",
                    "embabel-docs"),
            new DocChunk("d2", "类型化建模",
                    "类型化建模指用显式的领域类型描述输入、输出与中间产物。Embabel 依赖类型"
                            + "推导动作之间的依赖关系，因此类型既是契约也是规划依据。",
                    "embabel-docs"),
            new DocChunk("d3", "GOAP 规划器",
                    "GOAP（目标导向行动规划）通过动作的前置条件与后置条件搜索出一条从初始状态"
                            + "到目标的路径。它是 Embabel 默认的规划方式。",
                    "embabel-docs"),
            new DocChunk("d4", "黑板与共享上下文",
                    "黑板是 Agent 进程内的共享上下文，存放所有已产生的对象。动作从黑板读取输入、"
                            + "把输出写回黑板，从而实现动作之间的数据传递。",
                    "embabel-docs"),
            new DocChunk("d5", "工具循环与停止条件",
                    "工具循环指模型反复「选择工具 -> 执行 -> 观察结果」的过程。必须设置停止条件"
                            + "（最大迭代次数、目标达成、显式终止），否则可能无限循环。",
                    "embabel-docs"),
            new DocChunk("d6", "RAG 的成本控制",
                    "RAG 的收益来自「只把相关片段放进提示词」。片段数 k 与分块大小共同决定 token"
                            + "成本；k 过大不仅更贵，还可能引入噪声反而降低准确率。",
                    "internal-wiki"),
            new DocChunk("d7", "向量检索的召回与重排",
                    "两阶段检索：先用向量做粗召回（快、覆盖广），再用交叉编码器或 LLM 做精排"
                            + "（准、慢）。只做召回时，top-1 常常不是最相关的。",
                    "internal-wiki"),
            new DocChunk("d8", "pgvector 使用要点",
                    "pgvector 用 vector(n) 存储向量，<=> 是余弦距离、<-> 是 L2、<#> 是内积。"
                            + "建 HNSW 索引可加速；过滤条件写在 WHERE 里即可与向量排序组合使用。",
                    "internal-wiki"));
}
