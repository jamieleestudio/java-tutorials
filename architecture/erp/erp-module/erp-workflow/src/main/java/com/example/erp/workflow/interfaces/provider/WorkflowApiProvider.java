package com.example.erp.workflow.interfaces.provider;

import com.example.erp.workflow.api.WorkflowApi;
import com.example.erp.workflow.api.dto.WorkflowDto;
import com.example.erp.workflow.application.WorkflowApplicationService;
import org.springframework.stereotype.Component;

@Component
public class WorkflowApiProvider implements WorkflowApi {

    private final WorkflowApplicationService applicationService;

    public WorkflowApiProvider(WorkflowApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public WorkflowDto findById(String id) {
        return applicationService.findById(id);
    }
}