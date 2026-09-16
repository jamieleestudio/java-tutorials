package com.example.erp.integration.application;

import com.example.erp.integration.api.IntegrationApi;
import com.example.erp.integration.api.dto.IntegrationDto;
import com.example.erp.integration.domain.model.Integration;
import com.example.erp.integration.domain.repository.IntegrationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IntegrationApplicationService implements IntegrationApi {

    private final IntegrationRepository repository;
    private final SystemApi systemApi;

    public IntegrationApplicationService(IntegrationRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public IntegrationDto findById(String id) {
        systemApi.currentTenantId();
        Integration aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Integration not found: " + id));
        return new IntegrationDto(aggregate.id(), aggregate.name());
    }
}