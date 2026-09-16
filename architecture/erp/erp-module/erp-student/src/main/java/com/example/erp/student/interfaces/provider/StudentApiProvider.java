package com.example.erp.student.interfaces.provider;

import com.example.erp.student.api.StudentApi;
import com.example.erp.student.api.dto.StudentDto;
import com.example.erp.student.application.StudentApplicationService;
import org.springframework.stereotype.Component;

@Component
public class StudentApiProvider implements StudentApi {

    private final StudentApplicationService applicationService;

    public StudentApiProvider(StudentApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public StudentDto findById(String id) {
        return applicationService.findById(id);
    }
}