package com.example.erp.dormitory.interfaces.web;

import com.example.erp.dormitory.application.DormitoryApplicationService;
import com.example.erp.dormitory.interfaces.web.dto.DormitoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dormitory")
public class DormitoryController {

    private final DormitoryApplicationService applicationService;

    public DormitoryController(DormitoryApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public DormitoryResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new DormitoryResponse(dto.id(), dto.name());
    }
}