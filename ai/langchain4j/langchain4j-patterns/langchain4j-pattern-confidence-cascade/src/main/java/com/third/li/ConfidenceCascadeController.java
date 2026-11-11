package com.third.li;

import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 置信度级联接口：GET /patterns/cascade?request=… */
@RestController
public class ConfidenceCascadeController {

    private final UntypedAgent confidenceCascade;

    public ConfidenceCascadeController(UntypedAgent confidenceCascade) {
        this.confidenceCascade = confidenceCascade;
    }

    /** 便宜模型先答；决策模型置信度不足（<0.6）时自动升级到贵模型重答。 */
    @GetMapping("/patterns/cascade")
    public Map<String, Object> ask(
            @RequestParam(defaultValue = "1+1 等于几？") String request) {
        Object answer = confidenceCascade.invoke(Map.of("request", request));
        return Map.of("request", request, "answer", String.valueOf(answer));
    }
}
