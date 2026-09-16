package com.example.erp.exam.application;

import com.example.erp.exam.api.ExamQueryService;
import com.example.erp.exam.api.dto.ExamDto;
import com.example.erp.exam.domain.model.Exam;
import com.example.erp.exam.domain.repository.ExamRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExamQueryServiceApplicationService implements ExamQueryService {

    private final ExamRepository repository;
    private final SystemQueryService systemQueryService;

    public ExamQueryServiceApplicationService(ExamRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public ExamDto findById(String id) {
        systemQueryService.currentTenantId();
        Exam aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found: " + id));
        return new ExamDto(aggregate.id(), aggregate.name());
    }
}