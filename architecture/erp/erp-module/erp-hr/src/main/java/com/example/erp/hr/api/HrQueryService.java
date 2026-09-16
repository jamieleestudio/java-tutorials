package com.example.erp.hr.api;

import com.example.erp.hr.api.dto.EmployeeDto;

public interface HrQueryService {

    EmployeeDto findById(String id);
}