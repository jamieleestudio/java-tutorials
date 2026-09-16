package com.example.erp.grade.domain.repository;

import com.example.erp.grade.domain.model.Grade;

import java.util.List;
import java.util.Optional;

public interface GradeRepository {

    Grade save(Grade grade);

    Optional<Grade> findById(String id);

    List<Grade> findByStudentId(String studentId);
}