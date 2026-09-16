package com.example.erp.enrollment.interfaces.provider;

import com.example.erp.enrollment.api.EnrollmentApi;
import com.example.erp.enrollment.api.dto.EnrollmentDto;
import com.example.erp.enrollment.application.EnrollmentApplicationService;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentApiProvider implements EnrollmentApi {

    private final EnrollmentApplicationService applicationService;

    public EnrollmentApiProvider(EnrollmentApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public EnrollmentDto findById(String id) {
        return applicationService.findById(id);
    }
}