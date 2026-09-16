package com.example.erp.evaluation.api;

import com.example.erp.evaluation.api.dto.EvaluationDto;

public interface EvaluationApi {

    EvaluationDto findById(String id);
}