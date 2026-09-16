package com.example.erp.workflow.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowJpaRepository extends BaseJpaRepository<WorkflowEntity, String> {
}