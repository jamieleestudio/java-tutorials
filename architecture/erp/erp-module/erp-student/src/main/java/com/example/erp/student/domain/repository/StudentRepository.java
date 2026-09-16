package com.example.erp.student.domain.repository;

import com.example.erp.student.domain.model.Student;

import java.util.Optional;

public interface StudentRepository {

    Student save(Student aggregate);

    Optional<Student> findById(String id);
}