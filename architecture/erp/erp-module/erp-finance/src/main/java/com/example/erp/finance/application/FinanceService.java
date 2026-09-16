package com.example.erp.finance.application;

import com.example.erp.finance.api.FinanceQueryApi;
import com.example.erp.finance.api.dto.BillDto;
import com.example.erp.finance.domain.model.Bill;
import com.example.erp.finance.domain.repository.BillRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceService implements FinanceQueryApi {

    private final BillRepository repository;
    private final SystemQueryApi systemQueryApi;

    public FinanceService(BillRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public BillDto findById(String id) {
        systemQueryApi.currentTenantId();
        Bill aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found: " + id));
        return new BillDto(aggregate.id(), aggregate.name());
    }
}