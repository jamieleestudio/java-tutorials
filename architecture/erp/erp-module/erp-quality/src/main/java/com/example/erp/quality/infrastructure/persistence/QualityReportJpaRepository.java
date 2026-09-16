package com.example.erp.quality.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityReportJpaRepository extends BaseJpaRepository<QualityReportEntity, String> {
}