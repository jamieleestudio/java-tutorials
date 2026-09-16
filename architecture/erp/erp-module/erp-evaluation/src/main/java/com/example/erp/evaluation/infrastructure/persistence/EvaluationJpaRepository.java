package com.example.erp.evaluation.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationJpaRepository extends BaseJpaRepository<EvaluationEntity, String> {
}