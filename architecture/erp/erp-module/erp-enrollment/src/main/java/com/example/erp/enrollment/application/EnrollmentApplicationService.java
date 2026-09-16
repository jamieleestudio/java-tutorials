package com.example.erp.enrollment.application;

import com.example.erp.enrollment.api.EnrollmentApi;
import com.example.erp.enrollment.api.dto.EnrollmentDto;
import com.example.erp.enrollment.domain.model.Enrollment;
import com.example.erp.enrollment.domain.repository.EnrollmentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnrollmentApplicationService implements EnrollmentApi {

    private final EnrollmentRepository repository;
    private final SystemApi systemApi;

    public EnrollmentApplicationService(EnrollmentRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public EnrollmentDto findById(String id) {
        systemApi.currentTenantId();
        Enrollment aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + id));
        return new EnrollmentDto(aggregate.id(), aggregate.name());
    }
}