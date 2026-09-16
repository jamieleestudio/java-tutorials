package com.example.erp.employment.interfaces.web;

import com.example.erp.employment.application.EmploymentApplicationService;
import com.example.erp.employment.interfaces.web.dto.EmploymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employment")
public class EmploymentController {

    private final EmploymentApplicationService applicationService;

    public EmploymentController(EmploymentApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public EmploymentResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new EmploymentResponse(dto.id(), dto.name());
    }
}