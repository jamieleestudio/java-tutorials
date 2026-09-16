package com.example.erp.exam.domain.repository;

import com.example.erp.exam.domain.model.Exam;

import java.util.Optional;

public interface ExamRepository {

    Exam save(Exam aggregate);

    Optional<Exam> findById(String id);
}