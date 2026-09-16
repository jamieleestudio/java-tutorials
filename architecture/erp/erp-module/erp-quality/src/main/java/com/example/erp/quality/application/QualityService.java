package com.example.erp.quality.application;

import com.example.erp.quality.api.QualityQueryApi;
import com.example.erp.quality.api.dto.QualityReportDto;
import com.example.erp.quality.domain.model.QualityReport;
import com.example.erp.quality.domain.repository.QualityReportRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QualityService implements QualityQueryApi {

    private final QualityReportRepository repository;
    private final SystemQueryApi systemQueryApi;

    public QualityService(QualityReportRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public QualityReportDto findById(String id) {
        systemQueryApi.currentTenantId();
        QualityReport aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QualityReport not found: " + id));
        return new QualityReportDto(aggregate.id(), aggregate.name());
    }
}