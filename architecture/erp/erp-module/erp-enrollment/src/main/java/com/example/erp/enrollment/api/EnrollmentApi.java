package com.example.erp.enrollment.api;

import com.example.erp.enrollment.api.dto.EnrollmentDto;

public interface EnrollmentApi {

    EnrollmentDto findById(String id);
}