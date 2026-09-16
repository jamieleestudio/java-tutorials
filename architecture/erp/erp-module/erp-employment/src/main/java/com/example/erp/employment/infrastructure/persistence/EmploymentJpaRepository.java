package com.example.erp.employment.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmploymentJpaRepository extends BaseJpaRepository<EmploymentEntity, String> {
}