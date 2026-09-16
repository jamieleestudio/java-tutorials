package com.example.erp.quality.infrastructure.persistence;

import com.example.erp.quality.domain.model.QualityReport;
import com.example.erp.quality.domain.repository.QualityReportRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class QualityReportRepositoryImpl implements QualityReportRepository {

    private final QualityReportJpaRepository jpaRepository;

    public QualityReportRepositoryImpl(QualityReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public QualityReport save(QualityReport aggregate) {
        QualityReportEntity entity = new QualityReportEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        QualityReportEntity saved = jpaRepository.save(entity);
        return new QualityReport(saved.getId(), saved.getName());
    }

    @Override
    public Optional<QualityReport> findById(String id) {
        return jpaRepository.findById(id).map(e -> new QualityReport(e.getId(), e.getName()));
    }
}