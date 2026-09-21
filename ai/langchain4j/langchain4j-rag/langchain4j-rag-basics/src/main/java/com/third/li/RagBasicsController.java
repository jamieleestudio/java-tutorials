package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** RAG 问答接口：GET /ai/rag?question=… */
@RestController
public class RagBasicsController {

    private final RagBasicsConfig.Tutor tutor;

    public RagBasicsController(RagBasicsConfig.Tutor tutor) {
        this.tutor = tutor;
    }

    @GetMapping("/ai/rag")
    public String answer(@RequestParam(defaultValue = "AgenticScope 是什么？") String question) {
        return tutor.answer(question);
    }
}
