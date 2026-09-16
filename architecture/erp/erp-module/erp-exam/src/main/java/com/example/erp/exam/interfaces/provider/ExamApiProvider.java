package com.example.erp.exam.interfaces.provider;

import com.example.erp.exam.api.ExamApi;
import com.example.erp.exam.api.dto.ExamDto;
import com.example.erp.exam.application.ExamApplicationService;
import org.springframework.stereotype.Component;

@Component
public class ExamApiProvider implements ExamApi {

    private final ExamApplicationService applicationService;

    public ExamApiProvider(ExamApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public ExamDto findById(String id) {
        return applicationService.findById(id);
    }
}