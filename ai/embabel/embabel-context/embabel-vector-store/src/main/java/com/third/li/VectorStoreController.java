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

/**
 * pgvector 向量库接口。
 */
@RestController
public class VectorStoreController {

    private final AiBuilder aiBuilder;
    private final PgVectorStore store;
    private final AgentPlatform agentPlatform;

    public VectorStoreController(AiBuilder aiBuilder, PgVectorStore store, AgentPlatform agentPlatform) {
        this.aiBuilder = aiBuilder;
        this.store = store;
        this.agentPlatform = agentPlatform;
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
}
