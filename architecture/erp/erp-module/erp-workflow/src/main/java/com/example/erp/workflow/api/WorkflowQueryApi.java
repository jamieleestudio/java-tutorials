package com.example.erp.workflow.api;

import com.example.erp.workflow.api.dto.WorkflowDto;

public interface WorkflowQueryApi {

    WorkflowDto findById(String id);
}