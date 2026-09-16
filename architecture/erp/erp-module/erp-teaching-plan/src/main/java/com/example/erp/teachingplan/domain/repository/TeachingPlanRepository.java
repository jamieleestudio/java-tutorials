package com.example.erp.teachingplan.domain.repository;

import com.example.erp.teachingplan.domain.model.TeachingPlan;

import java.util.Optional;

public interface TeachingPlanRepository {

    TeachingPlan save(TeachingPlan aggregate);

    Optional<TeachingPlan> findById(String id);
}