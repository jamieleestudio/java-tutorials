package com.example.erp.employment.api;

import com.example.erp.employment.api.dto.EmploymentDto;

public interface EmploymentQueryService {

    EmploymentDto findById(String id);
}