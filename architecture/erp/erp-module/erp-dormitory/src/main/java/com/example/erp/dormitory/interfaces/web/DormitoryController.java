package com.example.erp.dormitory.interfaces.web;

import com.example.erp.dormitory.api.DormitoryQueryApi;
import com.example.erp.dormitory.interfaces.web.dto.DormitoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dormitory")
public class DormitoryController {

    private final DormitoryQueryApi queryService;

    public DormitoryController(DormitoryQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public DormitoryResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new DormitoryResponse(dto.id(), dto.name());
    }
}