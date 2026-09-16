package com.example.erp.teachingplan.application;

import com.example.erp.teachingplan.api.TeachingPlanQueryApi;
import com.example.erp.teachingplan.api.dto.TeachingPlanDto;
import com.example.erp.teachingplan.domain.model.TeachingPlan;
import com.example.erp.teachingplan.domain.repository.TeachingPlanRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeachingPlanApplicationService implements TeachingPlanQueryApi {

    private final TeachingPlanRepository repository;
    private final SystemQueryApi systemQueryApi;

    public TeachingPlanApplicationService(TeachingPlanRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public TeachingPlanDto findById(String id) {
        systemQueryApi.currentTenantId();
        TeachingPlan aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TeachingPlan not found: " + id));
        return new TeachingPlanDto(aggregate.id(), aggregate.name());
    }
}