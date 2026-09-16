package com.example.erp.asset.application;

import com.example.erp.asset.api.AssetQueryService;
import com.example.erp.asset.api.dto.AssetDto;
import com.example.erp.asset.domain.model.Asset;
import com.example.erp.asset.domain.repository.AssetRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetQueryServiceApplicationService implements AssetQueryService {

    private final AssetRepository repository;
    private final SystemQueryService systemQueryService;

    public AssetQueryServiceApplicationService(AssetRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public AssetDto findById(String id) {
        systemQueryService.currentTenantId();
        Asset aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
        return new AssetDto(aggregate.id(), aggregate.name());
    }
}