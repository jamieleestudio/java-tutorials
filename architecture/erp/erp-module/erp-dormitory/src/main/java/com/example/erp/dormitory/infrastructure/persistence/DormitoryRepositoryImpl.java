package com.example.erp.dormitory.infrastructure.persistence;

import com.example.erp.dormitory.domain.model.Dormitory;
import com.example.erp.dormitory.domain.repository.DormitoryRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DormitoryRepositoryImpl implements DormitoryRepository {

    private final DormitoryJpaRepository jpaRepository;

    public DormitoryRepositoryImpl(DormitoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Dormitory save(Dormitory aggregate) {
        DormitoryEntity entity = new DormitoryEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        DormitoryEntity saved = jpaRepository.save(entity);
        return new Dormitory(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Dormitory> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Dormitory(e.getId(), e.getName()));
    }
}