package com.example.erp.hr.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJpaRepository extends BaseJpaRepository<EmployeeEntity, String> {
}