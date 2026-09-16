package com.example.erp.grade.infrastructure.persistence;

import com.example.erp.grade.domain.model.Grade;
import com.example.erp.grade.domain.repository.GradeRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GradeRepositoryImpl implements GradeRepository {

    private final GradeJpaRepository jpaRepository;

    public GradeRepositoryImpl(GradeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Grade save(Grade grade) {
        GradeEntity entity = new GradeEntity();
        entity.setId(grade.id() == null ? IdGenerator.next() : grade.id());
        entity.setStudentId(grade.studentId());
        entity.setCourseName(grade.courseName());
        entity.setScore(grade.score());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Grade> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Grade> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream().map(this::toDomain).toList();
    }

    private Grade toDomain(GradeEntity entity) {
        return new Grade(entity.getId(), entity.getStudentId(), entity.getCourseName(), entity.getScore());
    }
}