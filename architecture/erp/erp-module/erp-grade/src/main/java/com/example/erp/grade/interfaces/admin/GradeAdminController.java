package com.example.erp.grade.interfaces.admin;

import com.example.erp.grade.api.GradeQueryService;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.interfaces.admin.dto.GradeAdminResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/grades")
public class GradeAdminController {

    private final GradeQueryService queryService;

    public GradeAdminController(GradeQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public GradeAdminResponse get(@PathVariable String id) {
        GradeDto dto = queryService.findById(id);
        return new GradeAdminResponse(dto.id(), dto.courseName(), dto.score());
    }
}