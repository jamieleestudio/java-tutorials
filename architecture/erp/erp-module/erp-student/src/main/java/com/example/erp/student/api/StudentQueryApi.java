package com.example.erp.student.api;

import com.example.erp.student.api.dto.StudentDto;

public interface StudentQueryApi {

    StudentDto findById(String id);
}