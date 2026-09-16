package com.example.erp.teachingplan.application;

import com.example.erp.teachingplan.api.TeachingPlanApi;
import com.example.erp.teachingplan.api.dto.TeachingPlanDto;
import com.example.erp.teachingplan.domain.model.TeachingPlan;
import com.example.erp.teachingplan.domain.repository.TeachingPlanRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeachingPlanApplicationService implements TeachingPlanApi {

    private final TeachingPlanRepository repository;
    private final SystemApi systemApi;

    public TeachingPlanApplicationService(TeachingPlanRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public TeachingPlanDto findById(String id) {
        systemApi.currentTenantId();
        TeachingPlan aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TeachingPlan not found: " + id));
        return new TeachingPlanDto(aggregate.id(), aggregate.name());
    }
}