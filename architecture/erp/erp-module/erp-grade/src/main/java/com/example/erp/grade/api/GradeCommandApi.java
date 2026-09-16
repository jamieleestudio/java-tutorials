package com.example.erp.grade.api;

import com.example.erp.grade.api.dto.GradeDto;

public interface GradeCommandApi {

    GradeDto create(String studentId, String courseName, double score);
}