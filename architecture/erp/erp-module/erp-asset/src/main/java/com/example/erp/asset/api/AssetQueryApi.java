package com.example.erp.asset.api;

import com.example.erp.asset.api.dto.AssetDto;

public interface AssetQueryApi {

    AssetDto findById(String id);
}