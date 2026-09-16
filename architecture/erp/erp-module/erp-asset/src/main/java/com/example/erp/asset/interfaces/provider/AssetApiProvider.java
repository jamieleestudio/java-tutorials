package com.example.erp.asset.interfaces.provider;

import com.example.erp.asset.api.AssetApi;
import com.example.erp.asset.api.dto.AssetDto;
import com.example.erp.asset.application.AssetApplicationService;
import org.springframework.stereotype.Component;

@Component
public class AssetApiProvider implements AssetApi {

    private final AssetApplicationService applicationService;

    public AssetApiProvider(AssetApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public AssetDto findById(String id) {
        return applicationService.findById(id);
    }
}