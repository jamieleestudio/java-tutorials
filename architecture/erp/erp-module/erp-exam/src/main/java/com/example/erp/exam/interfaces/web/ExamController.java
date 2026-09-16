package com.example.erp.exam.interfaces.web;

import com.example.erp.exam.api.ExamApi;
import com.example.erp.exam.interfaces.web.dto.ExamResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam")
public class ExamController {

    private final ExamApi queryService;

    public ExamController(ExamApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public ExamResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new ExamResponse(dto.id(), dto.name());
    }
}