package com.example.erp.teachingplan.interfaces.web;

import com.example.erp.teachingplan.application.TeachingPlanApplicationService;
import com.example.erp.teachingplan.interfaces.web.dto.TeachingPlanResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teaching-plan")
public class TeachingPlanController {

    private final TeachingPlanApplicationService applicationService;

    public TeachingPlanController(TeachingPlanApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public TeachingPlanResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new TeachingPlanResponse(dto.id(), dto.name());
    }
}