package com.example.erp.finance.interfaces.provider;

import com.example.erp.finance.api.FinanceApi;
import com.example.erp.finance.api.dto.BillDto;
import com.example.erp.finance.application.FinanceApplicationService;
import org.springframework.stereotype.Component;

@Component
public class FinanceApiProvider implements FinanceApi {

    private final FinanceApplicationService applicationService;

    public FinanceApiProvider(FinanceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public BillDto findById(String id) {
        return applicationService.findById(id);
    }
}