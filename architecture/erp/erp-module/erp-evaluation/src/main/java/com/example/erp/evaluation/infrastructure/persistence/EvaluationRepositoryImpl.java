package com.example.erp.evaluation.infrastructure.persistence;

import com.example.erp.evaluation.domain.model.Evaluation;
import com.example.erp.evaluation.domain.repository.EvaluationRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EvaluationRepositoryImpl implements EvaluationRepository {

    private final EvaluationJpaRepository jpaRepository;

    public EvaluationRepositoryImpl(EvaluationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Evaluation save(Evaluation aggregate) {
        EvaluationEntity entity = new EvaluationEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        EvaluationEntity saved = jpaRepository.save(entity);
        return new Evaluation(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Evaluation> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Evaluation(e.getId(), e.getName()));
    }
}