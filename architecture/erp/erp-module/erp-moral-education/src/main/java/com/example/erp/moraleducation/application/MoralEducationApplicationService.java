package com.example.erp.moraleducation.application;

import com.example.erp.moraleducation.api.MoralEducationApi;
import com.example.erp.moraleducation.api.dto.MoralActivityDto;
import com.example.erp.moraleducation.domain.model.MoralActivity;
import com.example.erp.moraleducation.domain.repository.MoralActivityRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MoralEducationApplicationService implements MoralEducationApi {

    private final MoralActivityRepository repository;
    private final SystemApi systemApi;

    public MoralEducationApplicationService(MoralActivityRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public MoralActivityDto findById(String id) {
        systemApi.currentTenantId();
        MoralActivity aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MoralActivity not found: " + id));
        return new MoralActivityDto(aggregate.id(), aggregate.name());
    }
}