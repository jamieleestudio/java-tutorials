package com.example.erp.evaluation.interfaces.provider;

import com.example.erp.evaluation.api.EvaluationApi;
import com.example.erp.evaluation.api.dto.EvaluationDto;
import com.example.erp.evaluation.application.EvaluationApplicationService;
import org.springframework.stereotype.Component;

@Component
public class EvaluationApiProvider implements EvaluationApi {

    private final EvaluationApplicationService applicationService;

    public EvaluationApiProvider(EvaluationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public EvaluationDto findById(String id) {
        return applicationService.findById(id);
    }
}