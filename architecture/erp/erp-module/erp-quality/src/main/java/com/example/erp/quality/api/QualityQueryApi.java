package com.example.erp.quality.api;

import com.example.erp.quality.api.dto.QualityReportDto;

public interface QualityQueryApi {

    QualityReportDto findById(String id);
}