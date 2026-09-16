package com.example.erp.employment.api;

import com.example.erp.employment.api.dto.EmploymentDto;

public interface EmploymentQueryApi {

    EmploymentDto findById(String id);
}