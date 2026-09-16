package com.example.erp.integration.infrastructure.persistence;

import com.example.erp.integration.domain.model.Integration;
import com.example.erp.integration.domain.repository.IntegrationRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IntegrationRepositoryImpl implements IntegrationRepository {

    private final IntegrationJpaRepository jpaRepository;

    public IntegrationRepositoryImpl(IntegrationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Integration save(Integration aggregate) {
        IntegrationEntity entity = new IntegrationEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        IntegrationEntity saved = jpaRepository.save(entity);
        return new Integration(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Integration> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Integration(e.getId(), e.getName()));
    }
}