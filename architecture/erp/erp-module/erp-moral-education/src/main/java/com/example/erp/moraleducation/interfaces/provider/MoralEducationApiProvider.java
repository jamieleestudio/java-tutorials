package com.example.erp.moraleducation.interfaces.provider;

import com.example.erp.moraleducation.api.MoralEducationApi;
import com.example.erp.moraleducation.api.dto.MoralActivityDto;
import com.example.erp.moraleducation.application.MoralEducationApplicationService;
import org.springframework.stereotype.Component;

@Component
public class MoralEducationApiProvider implements MoralEducationApi {

    private final MoralEducationApplicationService applicationService;

    public MoralEducationApiProvider(MoralEducationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public MoralActivityDto findById(String id) {
        return applicationService.findById(id);
    }
}