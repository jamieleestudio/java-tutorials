package com.example.erp.enrollment.infrastructure.persistence;

import com.example.erp.enrollment.domain.model.Enrollment;
import com.example.erp.enrollment.domain.repository.EnrollmentRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private final EnrollmentJpaRepository jpaRepository;

    public EnrollmentRepositoryImpl(EnrollmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Enrollment save(Enrollment aggregate) {
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        EnrollmentEntity saved = jpaRepository.save(entity);
        return new Enrollment(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Enrollment> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Enrollment(e.getId(), e.getName()));
    }
}