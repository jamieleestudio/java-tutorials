package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 进阶接口。
 *
 * GET /ai/rag/advanced/product?question=… — 路由到产品知识库
 * GET /ai/rag/advanced/policy?question=… — 路由到政策知识库
 */
@RestController
public class RagAdvancedController {

    private final RagAdvancedConfig.KnowledgeAssistant assistant;

    public RagAdvancedController(RagAdvancedConfig.KnowledgeAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/rag/advanced/product")
    public String product(@RequestParam(defaultValue = "BGE 嵌入模型需要 API key 吗？") String question) {
        return assistant.answer(question);
    }

    @GetMapping("/ai/rag/advanced/policy")
    public String policy(@RequestParam(defaultValue = "教程仓库可以用在生产环境吗？") String question) {
        return assistant.answer(question);
    }
}
