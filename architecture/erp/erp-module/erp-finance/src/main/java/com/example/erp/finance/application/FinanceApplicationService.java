package com.example.erp.finance.application;

import com.example.erp.finance.api.dto.BillDto;
import com.example.erp.finance.domain.model.Bill;
import com.example.erp.finance.domain.repository.BillRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceApplicationService {

    private final BillRepository repository;
    private final SystemApi systemApi;

    public FinanceApplicationService(BillRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public BillDto findById(String id) {
        systemApi.currentTenantId();
        Bill aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found: " + id));
        return new BillDto(aggregate.id(), aggregate.name());
    }
}