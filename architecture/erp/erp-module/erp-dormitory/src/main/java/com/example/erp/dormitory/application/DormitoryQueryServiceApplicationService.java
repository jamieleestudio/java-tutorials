package com.example.erp.dormitory.application;

import com.example.erp.dormitory.api.DormitoryQueryService;
import com.example.erp.dormitory.api.dto.DormitoryDto;
import com.example.erp.dormitory.domain.model.Dormitory;
import com.example.erp.dormitory.domain.repository.DormitoryRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DormitoryQueryServiceApplicationService implements DormitoryQueryService {

    private final DormitoryRepository repository;
    private final SystemQueryService systemQueryService;

    public DormitoryQueryServiceApplicationService(DormitoryRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public DormitoryDto findById(String id) {
        systemQueryService.currentTenantId();
        Dormitory aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dormitory not found: " + id));
        return new DormitoryDto(aggregate.id(), aggregate.name());
    }
}