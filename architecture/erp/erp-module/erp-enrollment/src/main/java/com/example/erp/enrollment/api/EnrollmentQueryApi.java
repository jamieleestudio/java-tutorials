package com.example.erp.enrollment.api;

import com.example.erp.enrollment.api.dto.EnrollmentDto;

public interface EnrollmentQueryApi {

    EnrollmentDto findById(String id);
}