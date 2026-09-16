package com.example.erp.evaluation.application;

import com.example.erp.evaluation.api.EvaluationQueryApi;
import com.example.erp.evaluation.api.dto.EvaluationDto;
import com.example.erp.evaluation.domain.model.Evaluation;
import com.example.erp.evaluation.domain.repository.EvaluationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EvaluationApplicationService implements EvaluationQueryApi {

    private final EvaluationRepository repository;
    private final SystemQueryApi systemQueryApi;

    public EvaluationApplicationService(EvaluationRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public EvaluationDto findById(String id) {
        systemQueryApi.currentTenantId();
        Evaluation aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + id));
        return new EvaluationDto(aggregate.id(), aggregate.name());
    }
}