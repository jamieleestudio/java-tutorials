package com.example.erp.evaluation.application;

import com.example.erp.evaluation.api.dto.EvaluationDto;
import com.example.erp.evaluation.domain.model.Evaluation;
import com.example.erp.evaluation.domain.repository.EvaluationRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EvaluationApplicationService {

    private final EvaluationRepository repository;
    private final SystemApi systemApi;

    public EvaluationApplicationService(EvaluationRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public EvaluationDto findById(String id) {
        systemApi.currentTenantId();
        Evaluation aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + id));
        return new EvaluationDto(aggregate.id(), aggregate.name());
    }
}