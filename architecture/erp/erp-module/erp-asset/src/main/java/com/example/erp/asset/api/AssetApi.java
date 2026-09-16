package com.example.erp.asset.api;

import com.example.erp.asset.api.dto.AssetDto;

public interface AssetApi {

    AssetDto findById(String id);
}