package com.example.erp.moraleducation.interfaces.web;

import com.example.erp.moraleducation.api.MoralEducationQueryService;
import com.example.erp.moraleducation.interfaces.web.dto.MoralActivityResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/moral-education")
public class MoralActivityController {

    private final MoralEducationQueryService queryService;

    public MoralActivityController(MoralEducationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public MoralActivityResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new MoralActivityResponse(dto.id(), dto.name());
    }
}