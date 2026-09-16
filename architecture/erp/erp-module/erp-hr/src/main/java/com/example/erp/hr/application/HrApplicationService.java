package com.example.erp.hr.application;

import com.example.erp.hr.api.HrQueryApi;
import com.example.erp.hr.api.dto.EmployeeDto;
import com.example.erp.hr.domain.model.Employee;
import com.example.erp.hr.domain.repository.EmployeeRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HrApplicationService implements HrQueryApi {

    private final EmployeeRepository repository;
    private final SystemQueryApi systemQueryApi;

    public HrApplicationService(EmployeeRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public EmployeeDto findById(String id) {
        systemQueryApi.currentTenantId();
        Employee aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        return new EmployeeDto(aggregate.id(), aggregate.name());
    }
}