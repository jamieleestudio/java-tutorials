package com.example.erp.grade.interfaces.admin;

import com.example.erp.grade.application.GradeApplicationService;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.interfaces.admin.dto.GradeAdminResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/grades")
public class GradeAdminController {

    private final GradeApplicationService gradeApplicationService;

    public GradeAdminController(GradeApplicationService gradeApplicationService) {
        this.gradeApplicationService = gradeApplicationService;
    }

    @GetMapping("/{id}")
    public GradeAdminResponse get(@PathVariable String id) {
        GradeDto dto = gradeApplicationService.findById(id);
        return new GradeAdminResponse(dto.id(), dto.courseName(), dto.score());
    }
}