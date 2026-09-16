package com.example.erp.enrollment.domain.repository;

import com.example.erp.enrollment.domain.model.Enrollment;

import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment aggregate);

    Optional<Enrollment> findById(String id);
}