package com.example.erp.teachingplan.interfaces.provider;

import com.example.erp.teachingplan.api.TeachingPlanApi;
import com.example.erp.teachingplan.api.dto.TeachingPlanDto;
import com.example.erp.teachingplan.application.TeachingPlanApplicationService;
import org.springframework.stereotype.Component;

@Component
public class TeachingPlanApiProvider implements TeachingPlanApi {

    private final TeachingPlanApplicationService applicationService;

    public TeachingPlanApiProvider(TeachingPlanApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public TeachingPlanDto findById(String id) {
        return applicationService.findById(id);
    }
}