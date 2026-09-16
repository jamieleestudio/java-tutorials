package com.example.erp.quality.interfaces.web;

import com.example.erp.quality.api.QualityQueryService;
import com.example.erp.quality.interfaces.web.dto.QualityReportResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quality")
public class QualityReportController {

    private final QualityQueryService queryService;

    public QualityReportController(QualityQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public QualityReportResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new QualityReportResponse(dto.id(), dto.name());
    }
}