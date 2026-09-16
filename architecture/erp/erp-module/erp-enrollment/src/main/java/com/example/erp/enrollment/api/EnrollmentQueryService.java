package com.example.erp.enrollment.api;

import com.example.erp.enrollment.api.dto.EnrollmentDto;

public interface EnrollmentQueryService {

    EnrollmentDto findById(String id);
}