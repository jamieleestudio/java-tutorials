package com.example.erp.hr.api;

import com.example.erp.hr.api.dto.EmployeeDto;

public interface HrQueryApi {

    EmployeeDto findById(String id);
}