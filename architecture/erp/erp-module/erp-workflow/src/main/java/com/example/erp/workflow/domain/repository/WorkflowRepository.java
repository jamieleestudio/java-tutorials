package com.example.erp.workflow.domain.repository;

import com.example.erp.workflow.domain.model.Workflow;

import java.util.Optional;

public interface WorkflowRepository {

    Workflow save(Workflow aggregate);

    Optional<Workflow> findById(String id);
}