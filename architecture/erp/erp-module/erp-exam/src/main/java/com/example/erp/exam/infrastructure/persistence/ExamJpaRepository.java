package com.example.erp.exam.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamJpaRepository extends BaseJpaRepository<ExamEntity, String> {
}