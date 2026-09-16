package com.example.erp.dormitory.application;

import com.example.erp.dormitory.api.DormitoryApi;
import com.example.erp.dormitory.api.dto.DormitoryDto;
import com.example.erp.dormitory.domain.model.Dormitory;
import com.example.erp.dormitory.domain.repository.DormitoryRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DormitoryApplicationService implements DormitoryApi {

    private final DormitoryRepository repository;
    private final SystemApi systemApi;

    public DormitoryApplicationService(DormitoryRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public DormitoryDto findById(String id) {
        systemApi.currentTenantId();
        Dormitory aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dormitory not found: " + id));
        return new DormitoryDto(aggregate.id(), aggregate.name());
    }
}