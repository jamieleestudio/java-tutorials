package com.example.erp.integration.interfaces.provider;

import com.example.erp.integration.api.IntegrationApi;
import com.example.erp.integration.api.dto.IntegrationDto;
import com.example.erp.integration.application.IntegrationApplicationService;
import org.springframework.stereotype.Component;

@Component
public class IntegrationApiProvider implements IntegrationApi {

    private final IntegrationApplicationService applicationService;

    public IntegrationApiProvider(IntegrationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public IntegrationDto findById(String id) {
        return applicationService.findById(id);
    }
}