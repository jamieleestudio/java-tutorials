package com.example.erp.grade.api;

import com.example.erp.grade.api.dto.GradeDto;

import java.util.List;

public interface GradeQueryApi {

    GradeDto findById(String id);

    List<GradeDto> findByStudentId(String studentId);
}