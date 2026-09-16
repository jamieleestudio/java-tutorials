package com.example.erp.grade.api;

import com.example.erp.grade.api.dto.GradeDto;

import java.util.List;

public interface GradeApi {

    GradeDto findById(String id);

    List<GradeDto> findByStudentId(String studentId);

    GradeDto create(String studentId, String courseName, double score);
}
