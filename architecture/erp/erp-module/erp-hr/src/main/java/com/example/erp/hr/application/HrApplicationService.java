package com.example.erp.hr.application;

import com.example.erp.hr.api.dto.EmployeeDto;
import com.example.erp.hr.domain.model.Employee;
import com.example.erp.hr.domain.repository.EmployeeRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HrApplicationService {

    private final EmployeeRepository repository;
    private final SystemApi systemApi;

    public HrApplicationService(EmployeeRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public EmployeeDto findById(String id) {
        systemApi.currentTenantId();
        Employee aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
        return new EmployeeDto(aggregate.id(), aggregate.name());
    }
}