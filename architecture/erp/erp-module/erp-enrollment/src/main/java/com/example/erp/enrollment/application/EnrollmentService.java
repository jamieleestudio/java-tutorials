package com.example.erp.enrollment.application;

import com.example.erp.enrollment.api.EnrollmentQueryApi;
import com.example.erp.enrollment.api.dto.EnrollmentDto;
import com.example.erp.enrollment.domain.model.Enrollment;
import com.example.erp.enrollment.domain.repository.EnrollmentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnrollmentService implements EnrollmentQueryApi {

    private final EnrollmentRepository repository;
    private final SystemQueryApi systemQueryApi;

    public EnrollmentService(EnrollmentRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public EnrollmentDto findById(String id) {
        systemQueryApi.currentTenantId();
        Enrollment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + id));
        return new EnrollmentDto(aggregate.id(), aggregate.name());
    }
}