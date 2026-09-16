package com.example.erp.teachingplan.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachingPlanJpaRepository extends BaseJpaRepository<TeachingPlanEntity, String> {
}