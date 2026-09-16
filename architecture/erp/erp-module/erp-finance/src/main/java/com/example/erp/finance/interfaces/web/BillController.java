package com.example.erp.finance.interfaces.web;

import com.example.erp.finance.application.FinanceApplicationService;
import com.example.erp.finance.interfaces.web.dto.BillResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance")
public class BillController {

    private final FinanceApplicationService applicationService;

    public BillController(FinanceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public BillResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new BillResponse(dto.id(), dto.name());
    }
}