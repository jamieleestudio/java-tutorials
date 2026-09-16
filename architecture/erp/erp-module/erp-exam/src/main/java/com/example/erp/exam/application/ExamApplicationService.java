package com.example.erp.exam.application;

import com.example.erp.exam.api.dto.ExamDto;
import com.example.erp.exam.domain.model.Exam;
import com.example.erp.exam.domain.repository.ExamRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExamApplicationService {

    private final ExamRepository repository;
    private final SystemApi systemApi;

    public ExamApplicationService(ExamRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public ExamDto findById(String id) {
        systemApi.currentTenantId();
        Exam aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found: " + id));
        return new ExamDto(aggregate.id(), aggregate.name());
    }
}