package com.example.erp.hr.interfaces.web;

import com.example.erp.hr.application.HrApplicationService;
import com.example.erp.hr.interfaces.web.dto.EmployeeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr")
public class EmployeeController {

    private final HrApplicationService applicationService;

    public EmployeeController(HrApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new EmployeeResponse(dto.id(), dto.name());
    }
}