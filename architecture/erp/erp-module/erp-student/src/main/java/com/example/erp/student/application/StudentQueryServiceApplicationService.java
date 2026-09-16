package com.example.erp.student.application;

import com.example.erp.student.api.StudentQueryService;
import com.example.erp.student.api.dto.StudentDto;
import com.example.erp.student.domain.model.Student;
import com.example.erp.student.domain.repository.StudentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentQueryServiceApplicationService implements StudentQueryService {

    private final StudentRepository repository;
    private final SystemQueryService systemQueryService;

    public StudentQueryServiceApplicationService(StudentRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public StudentDto findById(String id) {
        systemQueryService.currentTenantId();
        Student aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        return new StudentDto(aggregate.id(), aggregate.name());
    }
}