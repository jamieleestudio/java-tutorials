package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.common.ai.model.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 本地模型接口：聊天 + 本地嵌入（都不需要云端 Key）。
 */
@RestController
public class OllamaController {

    private final AiBuilder aiBuilder;

    public OllamaController(AiBuilder aiBuilder) {
        this.aiBuilder = aiBuilder;
    }

    /** 本地模型聊天 */
    @GetMapping("/ollama/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "用一句话说明什么是本地大模型") String message) {
        PromptRunner runner = aiBuilder.ai().withDefaultLlm();
        return new Reply(runner.generateText(message));
    }

    /** 本地嵌入：返回维度与示例向量的前几个值 */
    @GetMapping("/ollama/embed")
    public Map<String, Object> embed(
            @RequestParam(value = "text", defaultValue = "本地嵌入模型") String text) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        float[] vector = embeddings.embed(text);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("model", "local-embedding");
        result.put("dimensions", embeddings.getDimensions());
        result.put("vectorLength", vector.length);
        result.put("sample", new float[]{vector[0], vector[1], vector[2]});
        return result;
    }
}
