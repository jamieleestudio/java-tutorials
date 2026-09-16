package com.example.erp.integration.interfaces.web;

import com.example.erp.integration.application.IntegrationApplicationService;
import com.example.erp.integration.interfaces.web.dto.IntegrationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {

    private final IntegrationApplicationService applicationService;

    public IntegrationController(IntegrationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public IntegrationResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new IntegrationResponse(dto.id(), dto.name());
    }
}