package com.example.erp.evaluation.domain.repository;

import com.example.erp.evaluation.domain.model.Evaluation;

import java.util.Optional;

public interface EvaluationRepository {

    Evaluation save(Evaluation aggregate);

    Optional<Evaluation> findById(String id);
}