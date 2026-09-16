package com.example.erp.evaluation.interfaces.web;

import com.example.erp.evaluation.api.EvaluationApi;
import com.example.erp.evaluation.interfaces.web.dto.EvaluationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationController {

    private final EvaluationApi queryService;

    public EvaluationController(EvaluationApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public EvaluationResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new EvaluationResponse(dto.id(), dto.name());
    }
}