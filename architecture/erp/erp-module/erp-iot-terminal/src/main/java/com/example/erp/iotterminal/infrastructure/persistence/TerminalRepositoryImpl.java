package com.example.erp.iotterminal.infrastructure.persistence;

import com.example.erp.iotterminal.domain.model.Terminal;
import com.example.erp.iotterminal.domain.repository.TerminalRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TerminalRepositoryImpl implements TerminalRepository {

    private final TerminalJpaRepository jpaRepository;

    public TerminalRepositoryImpl(TerminalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Terminal save(Terminal aggregate) {
        TerminalEntity entity = new TerminalEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        TerminalEntity saved = jpaRepository.save(entity);
        return new Terminal(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Terminal> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Terminal(e.getId(), e.getName()));
    }
}