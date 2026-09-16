package com.example.erp.employment.infrastructure.persistence;

import com.example.erp.employment.domain.model.Employment;
import com.example.erp.employment.domain.repository.EmploymentRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EmploymentRepositoryImpl implements EmploymentRepository {

    private final EmploymentJpaRepository jpaRepository;

    public EmploymentRepositoryImpl(EmploymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Employment save(Employment aggregate) {
        EmploymentEntity entity = new EmploymentEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        EmploymentEntity saved = jpaRepository.save(entity);
        return new Employment(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Employment> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Employment(e.getId(), e.getName()));
    }
}