package com.example.erp.evaluation.api;

import com.example.erp.evaluation.api.dto.EvaluationDto;

public interface EvaluationQueryService {

    EvaluationDto findById(String id);
}