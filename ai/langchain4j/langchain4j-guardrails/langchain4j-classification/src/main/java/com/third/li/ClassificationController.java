package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 分类接口：GET /ai/classification */
@RestController
public class ClassificationController {

    private final ClassificationConfig.SentimentAnalyzer analyzer;

    public ClassificationController(ClassificationConfig.SentimentAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @GetMapping("/ai/classification")
    public ClassificationConfig.Sentiment analyze(
            @RequestParam(defaultValue = "这个库的示例写得太清晰了，学起来毫不费力") String text) {
        return analyzer.analyze(text);
    }
}
