package com.example.erp.workflow.infrastructure.persistence;

import com.example.erp.workflow.domain.model.Workflow;
import com.example.erp.workflow.domain.repository.WorkflowRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WorkflowRepositoryImpl implements WorkflowRepository {

    private final WorkflowJpaRepository jpaRepository;

    public WorkflowRepositoryImpl(WorkflowJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Workflow save(Workflow aggregate) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        WorkflowEntity saved = jpaRepository.save(entity);
        return new Workflow(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Workflow> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Workflow(e.getId(), e.getName()));
    }
}