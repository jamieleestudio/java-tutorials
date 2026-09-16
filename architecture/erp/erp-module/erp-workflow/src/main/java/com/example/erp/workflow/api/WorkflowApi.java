package com.example.erp.workflow.api;

import com.example.erp.workflow.api.dto.WorkflowDto;

public interface WorkflowApi {

    WorkflowDto findById(String id);
}