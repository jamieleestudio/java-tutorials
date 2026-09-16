package com.example.erp.workflow.application;

import com.example.erp.workflow.api.WorkflowQueryService;
import com.example.erp.workflow.api.dto.WorkflowDto;
import com.example.erp.workflow.domain.model.Workflow;
import com.example.erp.workflow.domain.repository.WorkflowRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowQueryServiceApplicationService implements WorkflowQueryService {

    private final WorkflowRepository repository;
    private final SystemQueryService systemQueryService;

    public WorkflowQueryServiceApplicationService(WorkflowRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public WorkflowDto findById(String id) {
        systemQueryService.currentTenantId();
        Workflow aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + id));
        return new WorkflowDto(aggregate.id(), aggregate.name());
    }
}