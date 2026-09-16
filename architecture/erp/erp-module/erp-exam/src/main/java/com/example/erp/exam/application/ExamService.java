package com.example.erp.exam.application;

import com.example.erp.exam.api.ExamQueryApi;
import com.example.erp.exam.api.dto.ExamDto;
import com.example.erp.exam.domain.model.Exam;
import com.example.erp.exam.domain.repository.ExamRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExamService implements ExamQueryApi {

    private final ExamRepository repository;
    private final SystemQueryApi systemQueryApi;

    public ExamService(ExamRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public ExamDto findById(String id) {
        systemQueryApi.currentTenantId();
        Exam aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found: " + id));
        return new ExamDto(aggregate.id(), aggregate.name());
    }
}