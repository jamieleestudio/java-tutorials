package com.example.erp.quality.application;

import com.example.erp.quality.api.QualityApi;
import com.example.erp.quality.api.dto.QualityReportDto;
import com.example.erp.quality.domain.model.QualityReport;
import com.example.erp.quality.domain.repository.QualityReportRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QualityApplicationService implements QualityApi {

    private final QualityReportRepository repository;
    private final SystemApi systemApi;

    public QualityApplicationService(QualityReportRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public QualityReportDto findById(String id) {
        systemApi.currentTenantId();
        QualityReport aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QualityReport not found: " + id));
        return new QualityReportDto(aggregate.id(), aggregate.name());
    }
}