package com.example.erp.dormitory.application;

import com.example.erp.dormitory.api.DormitoryQueryApi;
import com.example.erp.dormitory.api.dto.DormitoryDto;
import com.example.erp.dormitory.domain.model.Dormitory;
import com.example.erp.dormitory.domain.repository.DormitoryRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DormitoryService implements DormitoryQueryApi {

    private final DormitoryRepository repository;
    private final SystemQueryApi systemQueryApi;

    public DormitoryService(DormitoryRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public DormitoryDto findById(String id) {
        systemQueryApi.currentTenantId();
        Dormitory aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dormitory not found: " + id));
        return new DormitoryDto(aggregate.id(), aggregate.name());
    }
}