package com.example.erp.enrollment.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentJpaRepository extends BaseJpaRepository<EnrollmentEntity, String> {
}