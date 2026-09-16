package com.example.erp.exam.api;

import com.example.erp.exam.api.dto.ExamDto;

public interface ExamQueryApi {

    ExamDto findById(String id);
}