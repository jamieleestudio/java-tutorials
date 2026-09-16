package com.example.erp.moraleducation.application;

import com.example.erp.moraleducation.api.MoralEducationQueryService;
import com.example.erp.moraleducation.api.dto.MoralActivityDto;
import com.example.erp.moraleducation.domain.model.MoralActivity;
import com.example.erp.moraleducation.domain.repository.MoralActivityRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MoralEducationQueryServiceApplicationService implements MoralEducationQueryService {

    private final MoralActivityRepository repository;
    private final SystemQueryService systemQueryService;

    public MoralEducationQueryServiceApplicationService(MoralActivityRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public MoralActivityDto findById(String id) {
        systemQueryService.currentTenantId();
        MoralActivity aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MoralActivity not found: " + id));
        return new MoralActivityDto(aggregate.id(), aggregate.name());
    }
}