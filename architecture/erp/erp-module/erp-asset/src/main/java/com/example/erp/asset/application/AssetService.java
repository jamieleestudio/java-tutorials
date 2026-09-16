package com.example.erp.asset.application;

import com.example.erp.asset.api.AssetQueryApi;
import com.example.erp.asset.api.dto.AssetDto;
import com.example.erp.asset.domain.model.Asset;
import com.example.erp.asset.domain.repository.AssetRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetService implements AssetQueryApi {

    private final AssetRepository repository;
    private final SystemQueryApi systemQueryApi;

    public AssetService(AssetRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public AssetDto findById(String id) {
        systemQueryApi.currentTenantId();
        Asset aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
        return new AssetDto(aggregate.id(), aggregate.name());
    }
}