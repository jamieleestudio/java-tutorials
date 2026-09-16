package com.example.erp.grade.application;

import com.example.erp.grade.api.GradeApi;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.domain.model.Grade;
import com.example.erp.grade.domain.repository.GradeRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.shared.IdGenerator;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GradeApplicationService implements GradeApi {

    private final GradeRepository repository;
    private final SystemApi systemApi;

    public GradeApplicationService(GradeRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    @Transactional
    public GradeDto create(String studentId, String courseName, double score) {
        systemApi.currentTenantId();
        Grade grade = new Grade(IdGenerator.next(), studentId, courseName, score);
        return toDto(repository.save(grade));
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDto findById(String id) {
        Grade grade = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found: " + id));
        return toDto(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDto> findByStudentId(String studentId) {
        return repository.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    private GradeDto toDto(Grade grade) {
        return new GradeDto(grade.id(), grade.studentId(), grade.courseName(), grade.score());
    }
}
