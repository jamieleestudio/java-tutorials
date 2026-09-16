package com.example.erp.quality.application;

import com.example.erp.quality.api.QualityQueryService;
import com.example.erp.quality.api.dto.QualityReportDto;
import com.example.erp.quality.domain.model.QualityReport;
import com.example.erp.quality.domain.repository.QualityReportRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QualityQueryServiceApplicationService implements QualityQueryService {

    private final QualityReportRepository repository;
    private final SystemQueryService systemQueryService;

    public QualityQueryServiceApplicationService(QualityReportRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public QualityReportDto findById(String id) {
        systemQueryService.currentTenantId();
        QualityReport aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QualityReport not found: " + id));
        return new QualityReportDto(aggregate.id(), aggregate.name());
    }
}