package com.example.erp.enrollment.application;

import com.example.erp.enrollment.api.EnrollmentQueryService;
import com.example.erp.enrollment.api.dto.EnrollmentDto;
import com.example.erp.enrollment.domain.model.Enrollment;
import com.example.erp.enrollment.domain.repository.EnrollmentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnrollmentQueryServiceApplicationService implements EnrollmentQueryService {

    private final EnrollmentRepository repository;
    private final SystemQueryService systemQueryService;

    public EnrollmentQueryServiceApplicationService(EnrollmentRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public EnrollmentDto findById(String id) {
        systemQueryService.currentTenantId();
        Enrollment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + id));
        return new EnrollmentDto(aggregate.id(), aggregate.name());
    }
}