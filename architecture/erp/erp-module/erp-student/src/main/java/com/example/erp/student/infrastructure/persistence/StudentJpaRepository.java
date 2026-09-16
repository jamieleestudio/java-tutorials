package com.example.erp.student.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentJpaRepository extends BaseJpaRepository<StudentEntity, String> {
}