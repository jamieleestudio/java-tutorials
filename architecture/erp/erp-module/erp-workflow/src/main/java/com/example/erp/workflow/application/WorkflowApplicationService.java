package com.example.erp.workflow.application;

import com.example.erp.workflow.api.dto.WorkflowDto;
import com.example.erp.workflow.domain.model.Workflow;
import com.example.erp.workflow.domain.repository.WorkflowRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowApplicationService {

    private final WorkflowRepository repository;
    private final SystemApi systemApi;

    public WorkflowApplicationService(WorkflowRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public WorkflowDto findById(String id) {
        systemApi.currentTenantId();
        Workflow aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + id));
        return new WorkflowDto(aggregate.id(), aggregate.name());
    }
}