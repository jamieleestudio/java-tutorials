package com.example.erp.integration.application;

import com.example.erp.integration.api.IntegrationQueryApi;
import com.example.erp.integration.api.dto.IntegrationDto;
import com.example.erp.integration.domain.model.Integration;
import com.example.erp.integration.domain.repository.IntegrationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IntegrationApplicationService implements IntegrationQueryApi {

    private final IntegrationRepository repository;
    private final SystemQueryApi systemQueryApi;

    public IntegrationApplicationService(IntegrationRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public IntegrationDto findById(String id) {
        systemQueryApi.currentTenantId();
        Integration aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Integration not found: " + id));
        return new IntegrationDto(aggregate.id(), aggregate.name());
    }
}