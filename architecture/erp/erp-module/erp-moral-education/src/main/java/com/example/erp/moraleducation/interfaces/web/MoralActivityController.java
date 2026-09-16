package com.example.erp.moraleducation.interfaces.web;

import com.example.erp.moraleducation.application.MoralEducationApplicationService;
import com.example.erp.moraleducation.interfaces.web.dto.MoralActivityResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/moral-education")
public class MoralActivityController {

    private final MoralEducationApplicationService applicationService;

    public MoralActivityController(MoralEducationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public MoralActivityResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new MoralActivityResponse(dto.id(), dto.name());
    }
}