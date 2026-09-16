package com.example.erp.finance.interfaces.web;

import com.example.erp.finance.api.FinanceQueryService;
import com.example.erp.finance.interfaces.web.dto.BillResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance")
public class BillController {

    private final FinanceQueryService queryService;

    public BillController(FinanceQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public BillResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new BillResponse(dto.id(), dto.name());
    }
}