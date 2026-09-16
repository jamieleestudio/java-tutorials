package com.example.erp.moraleducation.application;

import com.example.erp.moraleducation.api.MoralEducationQueryApi;
import com.example.erp.moraleducation.api.dto.MoralActivityDto;
import com.example.erp.moraleducation.domain.model.MoralActivity;
import com.example.erp.moraleducation.domain.repository.MoralActivityRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MoralEducationApplicationService implements MoralEducationQueryApi {

    private final MoralActivityRepository repository;
    private final SystemQueryApi systemQueryApi;

    public MoralEducationApplicationService(MoralActivityRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public MoralActivityDto findById(String id) {
        systemQueryApi.currentTenantId();
        MoralActivity aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MoralActivity not found: " + id));
        return new MoralActivityDto(aggregate.id(), aggregate.name());
    }
}