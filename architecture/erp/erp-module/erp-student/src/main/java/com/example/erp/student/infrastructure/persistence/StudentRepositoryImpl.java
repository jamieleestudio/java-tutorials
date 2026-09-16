package com.example.erp.student.infrastructure.persistence;

import com.example.erp.student.domain.model.Student;
import com.example.erp.student.domain.repository.StudentRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentJpaRepository jpaRepository;

    public StudentRepositoryImpl(StudentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Student save(Student aggregate) {
        StudentEntity entity = new StudentEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        StudentEntity saved = jpaRepository.save(entity);
        return new Student(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Student> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Student(e.getId(), e.getName()));
    }
}