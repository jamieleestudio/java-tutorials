package com.example.erp.exam.infrastructure.persistence;

import com.example.erp.exam.domain.model.Exam;
import com.example.erp.exam.domain.repository.ExamRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ExamRepositoryImpl implements ExamRepository {

    private final ExamJpaRepository jpaRepository;

    public ExamRepositoryImpl(ExamJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Exam save(Exam aggregate) {
        ExamEntity entity = new ExamEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        ExamEntity saved = jpaRepository.save(entity);
        return new Exam(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Exam> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Exam(e.getId(), e.getName()));
    }
}