package com.example.erp.exam.interfaces.web;

import com.example.erp.exam.application.ExamApplicationService;
import com.example.erp.exam.interfaces.web.dto.ExamResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam")
public class ExamController {

    private final ExamApplicationService applicationService;

    public ExamController(ExamApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public ExamResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new ExamResponse(dto.id(), dto.name());
    }
}