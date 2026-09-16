package com.example.erp.workflow.application;

import com.example.erp.workflow.api.WorkflowQueryApi;
import com.example.erp.workflow.api.dto.WorkflowDto;
import com.example.erp.workflow.domain.model.Workflow;
import com.example.erp.workflow.domain.repository.WorkflowRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowApplicationService implements WorkflowQueryApi {

    private final WorkflowRepository repository;
    private final SystemQueryApi systemQueryApi;

    public WorkflowApplicationService(WorkflowRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public WorkflowDto findById(String id) {
        systemQueryApi.currentTenantId();
        Workflow aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + id));
        return new WorkflowDto(aggregate.id(), aggregate.name());
    }
}