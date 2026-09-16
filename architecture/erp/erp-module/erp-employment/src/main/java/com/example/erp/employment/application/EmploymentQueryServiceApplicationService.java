package com.example.erp.employment.application;

import com.example.erp.employment.api.EmploymentQueryService;
import com.example.erp.employment.api.dto.EmploymentDto;
import com.example.erp.employment.domain.model.Employment;
import com.example.erp.employment.domain.repository.EmploymentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmploymentQueryServiceApplicationService implements EmploymentQueryService {

    private final EmploymentRepository repository;
    private final SystemQueryService systemQueryService;

    public EmploymentQueryServiceApplicationService(EmploymentRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public EmploymentDto findById(String id) {
        systemQueryService.currentTenantId();
        Employment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employment not found: " + id));
        return new EmploymentDto(aggregate.id(), aggregate.name());
    }
}