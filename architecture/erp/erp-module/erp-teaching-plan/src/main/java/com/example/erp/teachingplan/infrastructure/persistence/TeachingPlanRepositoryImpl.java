package com.example.erp.teachingplan.infrastructure.persistence;

import com.example.erp.teachingplan.domain.model.TeachingPlan;
import com.example.erp.teachingplan.domain.repository.TeachingPlanRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TeachingPlanRepositoryImpl implements TeachingPlanRepository {

    private final TeachingPlanJpaRepository jpaRepository;

    public TeachingPlanRepositoryImpl(TeachingPlanJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TeachingPlan save(TeachingPlan aggregate) {
        TeachingPlanEntity entity = new TeachingPlanEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        TeachingPlanEntity saved = jpaRepository.save(entity);
        return new TeachingPlan(saved.getId(), saved.getName());
    }

    @Override
    public Optional<TeachingPlan> findById(String id) {
        return jpaRepository.findById(id).map(e -> new TeachingPlan(e.getId(), e.getName()));
    }
}