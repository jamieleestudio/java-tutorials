package com.example.erp.evaluation.application;

import com.example.erp.evaluation.api.EvaluationQueryService;
import com.example.erp.evaluation.api.dto.EvaluationDto;
import com.example.erp.evaluation.domain.model.Evaluation;
import com.example.erp.evaluation.domain.repository.EvaluationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EvaluationQueryServiceApplicationService implements EvaluationQueryService {

    private final EvaluationRepository repository;
    private final SystemQueryService systemQueryService;

    public EvaluationQueryServiceApplicationService(EvaluationRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public EvaluationDto findById(String id) {
        systemQueryService.currentTenantId();
        Evaluation aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + id));
        return new EvaluationDto(aggregate.id(), aggregate.name());
    }
}