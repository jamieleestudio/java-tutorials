package com.example.erp.employment.domain.repository;

import com.example.erp.employment.domain.model.Employment;

import java.util.Optional;

public interface EmploymentRepository {

    Employment save(Employment aggregate);

    Optional<Employment> findById(String id);
}