package com.example.erp.moraleducation.infrastructure.persistence;

import com.example.erp.moraleducation.domain.model.MoralActivity;
import com.example.erp.moraleducation.domain.repository.MoralActivityRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MoralActivityRepositoryImpl implements MoralActivityRepository {

    private final MoralActivityJpaRepository jpaRepository;

    public MoralActivityRepositoryImpl(MoralActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MoralActivity save(MoralActivity aggregate) {
        MoralActivityEntity entity = new MoralActivityEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        MoralActivityEntity saved = jpaRepository.save(entity);
        return new MoralActivity(saved.getId(), saved.getName());
    }

    @Override
    public Optional<MoralActivity> findById(String id) {
        return jpaRepository.findById(id).map(e -> new MoralActivity(e.getId(), e.getName()));
    }
}