package com.example.erp.hr.interfaces.provider;

import com.example.erp.hr.api.HrApi;
import com.example.erp.hr.api.dto.EmployeeDto;
import com.example.erp.hr.application.HrApplicationService;
import org.springframework.stereotype.Component;

@Component
public class HrApiProvider implements HrApi {

    private final HrApplicationService applicationService;

    public HrApiProvider(HrApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public EmployeeDto findById(String id) {
        return applicationService.findById(id);
    }
}