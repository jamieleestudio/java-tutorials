package com.example.erp.student.api;

import com.example.erp.student.api.dto.StudentDto;

public interface StudentApi {

    StudentDto findById(String id);
}