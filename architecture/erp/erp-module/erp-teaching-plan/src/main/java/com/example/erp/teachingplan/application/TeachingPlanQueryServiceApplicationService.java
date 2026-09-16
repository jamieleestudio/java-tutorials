package com.example.erp.teachingplan.application;

import com.example.erp.teachingplan.api.TeachingPlanQueryService;
import com.example.erp.teachingplan.api.dto.TeachingPlanDto;
import com.example.erp.teachingplan.domain.model.TeachingPlan;
import com.example.erp.teachingplan.domain.repository.TeachingPlanRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeachingPlanQueryServiceApplicationService implements TeachingPlanQueryService {

    private final TeachingPlanRepository repository;
    private final SystemQueryService systemQueryService;

    public TeachingPlanQueryServiceApplicationService(TeachingPlanRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public TeachingPlanDto findById(String id) {
        systemQueryService.currentTenantId();
        TeachingPlan aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TeachingPlan not found: " + id));
        return new TeachingPlanDto(aggregate.id(), aggregate.name());
    }
}