package com.example.erp.quality.domain.repository;

import com.example.erp.quality.domain.model.QualityReport;

import java.util.Optional;

public interface QualityReportRepository {

    QualityReport save(QualityReport aggregate);

    Optional<QualityReport> findById(String id);
}