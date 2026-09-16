package com.example.erp.grade.interfaces.web;

import com.example.erp.grade.api.GradeApi;
import com.example.erp.grade.api.dto.GradeDto;
import com.example.erp.grade.interfaces.web.dto.CreateGradeWebRequest;
import com.example.erp.grade.interfaces.web.dto.GradeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeApi gradeApi;

    public GradeController(GradeApi gradeApi) {
        this.gradeApi = gradeApi;
    }

    @PostMapping
    public GradeResponse create(@RequestBody CreateGradeWebRequest request) {
        return toResponse(gradeApi.create(request.studentId(), request.courseName(), request.score()));
    }

    @GetMapping("/{id}")
    public GradeResponse get(@PathVariable String id) {
        return toResponse(gradeApi.findById(id));
    }

    @GetMapping
    public List<GradeResponse> byStudent(@RequestParam String studentId) {
        return gradeApi.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    private GradeResponse toResponse(GradeDto dto) {
        return new GradeResponse(dto.id(), dto.studentId(), dto.courseName(), dto.score());
    }
}
