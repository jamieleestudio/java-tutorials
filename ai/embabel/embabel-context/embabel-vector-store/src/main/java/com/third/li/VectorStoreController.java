package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * pgvector 向量库接口。
 */
@RestController
public class VectorStoreController {

    private final AiBuilder aiBuilder;
    private final PgVectorStore store;
    private final AgentPlatform agentPlatform;
    private final Reranker reranker;

    public VectorStoreController(AiBuilder aiBuilder, PgVectorStore store,
                                AgentPlatform agentPlatform, Reranker reranker) {
        this.aiBuilder = aiBuilder;
        this.store = store;
        this.agentPlatform = agentPlatform;
        this.reranker = reranker;
    }

    /** 把内置语料写入向量库（幂等，可重复调用）。 */
    @PostMapping("/vector/ingest")
    public Map<String, Object> ingest() {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        for (DocChunk doc : DocChunk.DOCUMENTS) {
            store.upsert(doc, embeddings.embed(doc.content()));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ingested", DocChunk.DOCUMENTS.size());
        result.put("total", store.count());
        result.put("dim", store.dim());
        return result;
    }

    /** 原始近邻检索（看得到相似度），可用 source 做元数据过滤。 */
    @GetMapping("/vector/search")
    public List<VectorMatch> search(
            @RequestParam("q") String q,
            @RequestParam(value = "k", defaultValue = "3") int k,
            @RequestParam(value = "source", required = false) String source) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        return store.search(embeddings.embed(q), k, source);
    }

    /** 向量库状态。 */
    @GetMapping("/vector/stats")
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", store.count());
        result.put("dim", store.dim());
        return result;
    }

    /** RAG：检索 + 生成（带相似度阈值）。 */
    @GetMapping("/vector/ask")
    public VectorAnswer ask(
            @RequestParam(value = "question", defaultValue = "pgvector 怎么做余弦相似度检索？") String question) {
        return AgentInvocation.create(agentPlatform, VectorAnswer.class).invoke(new UserInput(question));
    }

    /**
     * **两阶段检索对照**：同一批粗召回候选，"纯向量顺序" vs "LLM 重排后顺序"。
     *
     * <p>这是本模块最值得看的一个端点——它把"向量相似 ≠ 任务相关"直接摆出来。
     */
    @GetMapping("/vector/compare")
    public RerankComparison compare(
            @RequestParam(value = "question", defaultValue = "pgvector 怎么做余弦相似度检索？") String question,
            @RequestParam(value = "recall", defaultValue = "8") int recall) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        List<VectorMatch> recalled = store.search(embeddings.embed(question), recall, null);
        List<VectorMatch> reranked = reranker.rerank(question, recalled);
        return new RerankComparison(
                question,
                recalled,
                reranked,
                "第一阶段按余弦相似度排序；第二阶段让 LLM 按「与问题的相关性」重排。"
                        + "两者 score 语义不同，不可直接比较，看的是**顺序变化**。");
    }

    /**
     * 用**重排后的**前 k 条作为上下文生成回答。
     *
     * <p>与 {@code /vector/ask} 的区别：后者只用向量 top-k，这里先粗召回 {@code recall} 条再精排取 top-{@code k}。
     */
    @GetMapping("/vector/ask-reranked")
    public Map<String, Object> askReranked(
            @RequestParam(value = "question", defaultValue = "pgvector 怎么做余弦相似度检索？") String question,
            @RequestParam(value = "recall", defaultValue = "8") int recall,
            @RequestParam(value = "k", defaultValue = "3") int k) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        List<VectorMatch> recalled = store.search(embeddings.embed(question), recall, null);
        List<VectorMatch> top = reranker.rerank(question, recalled).stream().limit(k).toList();

        String context = top.stream()
                .map(match -> "- [%s/%s] %s".formatted(match.source(), match.id(), match.content()))
                .collect(Collectors.joining("\n"));
        String answer = aiBuilder.ai().withDefaultLlm()
                .withId("vector-answer-reranked")
                .generateText("""
                        请只依据下面的知识片段回答问题，并标注用到的片段来源（如 [internal-wiki/d7]）。

                        知识片段：
                        %s

                        问题：%s
                        """.formatted(context, question));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("question", question);
        out.put("recalled", recalled.size());
        out.put("usedChunks", top.stream().map(VectorMatch::id).toList());
        out.put("chunks", top);
        out.put("answer", answer);
        return out;
    }
}
