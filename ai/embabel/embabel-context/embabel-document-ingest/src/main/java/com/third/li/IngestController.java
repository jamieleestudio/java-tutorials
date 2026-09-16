package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档摄入接口。
 */
@RestController
public class IngestController {

    private final IngestService service;
    private final ChunkStore store;
    private final com.embabel.agent.api.common.AiBuilder aiBuilder;

    public IngestController(IngestService service, ChunkStore store,
                           com.embabel.agent.api.common.AiBuilder aiBuilder) {
        this.service = service;
        this.store = store;
        this.aiBuilder = aiBuilder;
    }

    /** 执行摄入（幂等：内容没变的文档会被跳过）。 */
    @PostMapping("/ingest/run")
    public IngestReport run() {
        return service.run();
    }

    /** 生成一个演示 PDF 并立即摄入（用来验证 PDF 抽取路径）。 */
    @PostMapping("/ingest/demo-pdf")
    public IngestReport demoPdf() {
        service.writeDemoPdf();
        return service.run();
    }

    /** 近邻检索，看得到相似度与出处（docId / seq）。 */
    @GetMapping("/ingest/search")
    public List<ChunkStore.ChunkHit> search(
            @RequestParam("q") String q,
            @RequestParam(value = "k", defaultValue = "3") int k) {
        float[] vector = aiBuilder.ai().withDefaultEmbeddingService().embed(q);
        return store.search(vector, k);
    }

    /** 当前状态：目录、分块参数、文档数与块数。 */
    @GetMapping("/ingest/stats")
    public Map<String, Object> stats() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dir", service.dirPath());
        out.put("documents", store.countDocuments());
        out.put("chunks", store.countChunks());
        out.put("dim", store.dim());
        return out;
    }
}
