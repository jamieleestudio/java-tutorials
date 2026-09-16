package com.example.erp.employment.interfaces.web;

import com.example.erp.employment.api.EmploymentQueryApi;
import com.example.erp.employment.interfaces.web.dto.EmploymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employment")
public class EmploymentController {

    private final EmploymentQueryApi queryService;

    public EmploymentController(EmploymentQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public EmploymentResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new EmploymentResponse(dto.id(), dto.name());
    }
}