package com.example.erp.asset.application;

import com.example.erp.asset.api.AssetApi;
import com.example.erp.asset.api.dto.AssetDto;
import com.example.erp.asset.domain.model.Asset;
import com.example.erp.asset.domain.repository.AssetRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetApplicationService implements AssetApi {

    private final AssetRepository repository;
    private final SystemApi systemApi;

    public AssetApplicationService(AssetRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    @Override
    public AssetDto findById(String id) {
        systemApi.currentTenantId();
        Asset aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
        return new AssetDto(aggregate.id(), aggregate.name());
    }
}