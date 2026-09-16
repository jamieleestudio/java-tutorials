package com.example.erp.employment.application;

import com.example.erp.employment.api.EmploymentQueryApi;
import com.example.erp.employment.api.dto.EmploymentDto;
import com.example.erp.employment.domain.model.Employment;
import com.example.erp.employment.domain.repository.EmploymentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmploymentService implements EmploymentQueryApi {

    private final EmploymentRepository repository;
    private final SystemQueryApi systemQueryApi;

    public EmploymentService(EmploymentRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public EmploymentDto findById(String id) {
        systemQueryApi.currentTenantId();
        Employment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employment not found: " + id));
        return new EmploymentDto(aggregate.id(), aggregate.name());
    }
}