package com.example.erp.employment.application;

import com.example.erp.employment.api.EmploymentApi;
import com.example.erp.employment.api.dto.EmploymentDto;
import com.example.erp.employment.domain.model.Employment;
import com.example.erp.employment.domain.repository.EmploymentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmploymentApplicationService implements EmploymentApi {

    private final EmploymentRepository repository;
    private final SystemApi systemApi;

    public EmploymentApplicationService(EmploymentRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public EmploymentDto findById(String id) {
        systemApi.currentTenantId();
        Employment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employment not found: " + id));
        return new EmploymentDto(aggregate.id(), aggregate.name());
    }
}