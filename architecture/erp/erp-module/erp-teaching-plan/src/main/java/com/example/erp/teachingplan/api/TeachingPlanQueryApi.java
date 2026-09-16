package com.example.erp.teachingplan.api;

import com.example.erp.teachingplan.api.dto.TeachingPlanDto;

public interface TeachingPlanQueryApi {

    TeachingPlanDto findById(String id);
}