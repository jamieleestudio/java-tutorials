package com.example.erp.employment.interfaces.provider;

import com.example.erp.employment.api.EmploymentApi;
import com.example.erp.employment.api.dto.EmploymentDto;
import com.example.erp.employment.application.EmploymentApplicationService;
import org.springframework.stereotype.Component;

@Component
public class EmploymentApiProvider implements EmploymentApi {

    private final EmploymentApplicationService applicationService;

    public EmploymentApiProvider(EmploymentApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public EmploymentDto findById(String id) {
        return applicationService.findById(id);
    }
}