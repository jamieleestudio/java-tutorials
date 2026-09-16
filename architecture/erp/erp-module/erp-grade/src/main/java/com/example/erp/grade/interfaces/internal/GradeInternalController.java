package com.example.erp.grade.interfaces.internal;

import com.example.erp.grade.api.GradeQueryService;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.interfaces.internal.dto.GradeInternalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/grades")
public class GradeInternalController {

    private final GradeQueryService queryService;

    public GradeInternalController(GradeQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public GradeInternalResponse get(@PathVariable String id) {
        GradeDto dto = queryService.findById(id);
        return new GradeInternalResponse(dto.id(), dto.studentId(), dto.courseName());
    }
}