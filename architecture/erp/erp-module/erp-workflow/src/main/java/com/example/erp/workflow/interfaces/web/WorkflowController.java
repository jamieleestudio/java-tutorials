package com.example.erp.workflow.interfaces.web;

import com.example.erp.workflow.api.WorkflowQueryApi;
import com.example.erp.workflow.interfaces.web.dto.WorkflowResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final WorkflowQueryApi queryService;

    public WorkflowController(WorkflowQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public WorkflowResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new WorkflowResponse(dto.id(), dto.name());
    }
}