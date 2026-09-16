package com.example.erp.teachingplan.api;

import com.example.erp.teachingplan.api.dto.TeachingPlanDto;

public interface TeachingPlanQueryService {

    TeachingPlanDto findById(String id);
}