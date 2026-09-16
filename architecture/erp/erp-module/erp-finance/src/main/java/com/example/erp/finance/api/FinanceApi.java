package com.example.erp.finance.api;

import com.example.erp.finance.api.dto.BillDto;

public interface FinanceApi {

    BillDto findById(String id);
}