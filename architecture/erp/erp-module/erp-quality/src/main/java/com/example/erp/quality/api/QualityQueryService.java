package com.example.erp.quality.api;

import com.example.erp.quality.api.dto.QualityReportDto;

public interface QualityQueryService {

    QualityReportDto findById(String id);
}