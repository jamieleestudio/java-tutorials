package com.example.erp.finance.infrastructure.persistence;

import com.example.erp.finance.domain.model.Bill;
import com.example.erp.finance.domain.repository.BillRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BillRepositoryImpl implements BillRepository {

    private final BillJpaRepository jpaRepository;

    public BillRepositoryImpl(BillJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Bill save(Bill aggregate) {
        BillEntity entity = new BillEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        BillEntity saved = jpaRepository.save(entity);
        return new Bill(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Bill> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Bill(e.getId(), e.getName()));
    }
}