package com.example.erp.grade.interfaces.provider;

import com.example.erp.grade.api.GradeApi;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.application.GradeApplicationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeApiProvider implements GradeApi {

    private final GradeApplicationService applicationService;

    public GradeApiProvider(GradeApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public GradeDto findById(String id) {
        return applicationService.findById(id);
    }

    @Override
    public List<GradeDto> findByStudentId(String studentId) {
        return applicationService.findByStudentId(studentId);
    }

    @Override
    public GradeDto create(String studentId, String courseName, double score) {
        return applicationService.create(studentId, courseName, score);
    }
}