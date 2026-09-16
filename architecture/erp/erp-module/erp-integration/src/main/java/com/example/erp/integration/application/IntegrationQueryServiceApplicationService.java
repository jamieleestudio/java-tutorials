package com.example.erp.integration.application;

import com.example.erp.integration.api.IntegrationQueryService;
import com.example.erp.integration.api.dto.IntegrationDto;
import com.example.erp.integration.domain.model.Integration;
import com.example.erp.integration.domain.repository.IntegrationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IntegrationQueryServiceApplicationService implements IntegrationQueryService {

    private final IntegrationRepository repository;
    private final SystemQueryService systemQueryService;

    public IntegrationQueryServiceApplicationService(IntegrationRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public IntegrationDto findById(String id) {
        systemQueryService.currentTenantId();
        Integration aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Integration not found: " + id));
        return new IntegrationDto(aggregate.id(), aggregate.name());
    }
}