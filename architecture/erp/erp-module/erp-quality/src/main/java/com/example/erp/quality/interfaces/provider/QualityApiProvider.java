package com.example.erp.quality.interfaces.provider;

import com.example.erp.quality.api.QualityApi;
import com.example.erp.quality.api.dto.QualityReportDto;
import com.example.erp.quality.application.QualityApplicationService;
import org.springframework.stereotype.Component;

@Component
public class QualityApiProvider implements QualityApi {

    private final QualityApplicationService applicationService;

    public QualityApiProvider(QualityApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public QualityReportDto findById(String id) {
        return applicationService.findById(id);
    }
}