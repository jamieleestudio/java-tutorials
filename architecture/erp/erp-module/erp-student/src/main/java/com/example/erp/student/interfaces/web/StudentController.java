package com.example.erp.student.interfaces.web;

import com.example.erp.student.api.StudentApi;
import com.example.erp.student.interfaces.web.dto.StudentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final StudentApi queryService;

    public StudentController(StudentApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new StudentResponse(dto.id(), dto.name());
    }
}