package com.third.li;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 向量存储（SimpleVectorStore 增删查）。
 *
 * <p>{@link VectorStore} = EmbeddingModel + 检索：
 * add() 时自动向量化，similaritySearch() 把查询向量化后按相似度召回。
 * SimpleVectorStore 是纯内存实现，适合教学；生产可换
 * AnalyticDB / Milvus / pgvector / Qdrant 等（SAA 对阿里云向量库有 starter）。
 */
@RestController
public class VectorStoreController {

    private final VectorStore vectorStore;

    public VectorStoreController(EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 预置知识
        vectorStore.add(List.of(
                new Document("Spring AI Alibaba 的 Graph 框架用 StateGraph 声明节点与边，支持 checkpoint 恢复。",
                        Map.of("topic", "graph")),
                new Document("Spring AI Alibaba 的 Agent Framework 提供 ReactAgent 与 Hook 体系。",
                        Map.of("topic", "agent")),
                new Document("通义千问 qwen-plus 通过 DashScope 的 OpenAI 兼容模式接入。",
                        Map.of("topic", "model"))));
    }

    /** 新增文档（自动向量化后入库）。 */
    @PostMapping("/ai/vector/add")
    public String add(@RequestParam("text") String text) {
        String id = UUID.randomUUID().toString();
        vectorStore.add(List.of(new Document(id, text, Map.of("source", "user"))));
        return "已入库，id=" + id;
    }

    /** 相似度检索。 */
    @GetMapping("/ai/vector/search")
    public List<Map<String, Object>> search(
            @RequestParam(value = "message", defaultValue = "Graph 怎么做状态恢复") String message,
            @RequestParam(value = "topK", defaultValue = "2") int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(topK).build())
                .stream()
                .map(doc -> Map.of("text", String.valueOf(doc.getText()), "meta", doc.getMetadata()))
                .toList();
    }

    /** 清空演示：删除一个文档后重新检索。 */
    @DeleteMapping("/ai/vector/clear")
    public String clear() {
        // SimpleVectorStore 按 id 删除；演示直接重建一个空库不方便，这里演示 delete 语义
        vectorStore.delete(List.of("not-exist-id"));
        return "delete(id) 语义演示完成：按 id 删除文档";
    }
}
