package com.example.erp.integration.interfaces.web;

import com.example.erp.integration.api.IntegrationApi;
import com.example.erp.integration.interfaces.web.dto.IntegrationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {

    private final IntegrationApi queryService;

    public IntegrationController(IntegrationApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public IntegrationResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new IntegrationResponse(dto.id(), dto.name());
    }
}