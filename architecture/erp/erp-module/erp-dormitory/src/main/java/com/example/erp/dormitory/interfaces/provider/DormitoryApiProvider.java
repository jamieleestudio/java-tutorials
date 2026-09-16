package com.example.erp.dormitory.interfaces.provider;

import com.example.erp.dormitory.api.DormitoryApi;
import com.example.erp.dormitory.api.dto.DormitoryDto;
import com.example.erp.dormitory.application.DormitoryApplicationService;
import org.springframework.stereotype.Component;

@Component
public class DormitoryApiProvider implements DormitoryApi {

    private final DormitoryApplicationService applicationService;

    public DormitoryApiProvider(DormitoryApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public DormitoryDto findById(String id) {
        return applicationService.findById(id);
    }
}