package com.example.erp.student.application;

import com.example.erp.student.api.StudentQueryApi;
import com.example.erp.student.api.dto.StudentDto;
import com.example.erp.student.domain.model.Student;
import com.example.erp.student.domain.repository.StudentRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentApplicationService implements StudentQueryApi {

    private final StudentRepository repository;
    private final SystemQueryApi systemQueryApi;

    public StudentApplicationService(StudentRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public StudentDto findById(String id) {
        systemQueryApi.currentTenantId();
        Student aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        return new StudentDto(aggregate.id(), aggregate.name());
    }
}