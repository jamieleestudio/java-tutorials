package com.example.erp.teachingplan.api;

import com.example.erp.teachingplan.api.dto.TeachingPlanDto;

public interface TeachingPlanApi {

    TeachingPlanDto findById(String id);
}