package com.example.erp.enrollment.interfaces.web;

import com.example.erp.enrollment.api.EnrollmentQueryService;
import com.example.erp.enrollment.interfaces.web.dto.EnrollmentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {

    private final EnrollmentQueryService queryService;

    public EnrollmentController(EnrollmentQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public EnrollmentResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new EnrollmentResponse(dto.id(), dto.name());
    }
}