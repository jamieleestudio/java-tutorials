package com.example.erp.student.api;

import com.example.erp.student.api.dto.StudentDto;

public interface StudentQueryService {

    StudentDto findById(String id);
}