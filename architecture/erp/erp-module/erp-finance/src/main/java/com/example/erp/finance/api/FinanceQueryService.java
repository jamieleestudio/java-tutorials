package com.example.erp.finance.api;

import com.example.erp.finance.api.dto.BillDto;

public interface FinanceQueryService {

    BillDto findById(String id);
}