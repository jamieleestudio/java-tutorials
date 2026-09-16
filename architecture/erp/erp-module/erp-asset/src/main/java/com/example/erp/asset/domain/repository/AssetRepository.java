package com.example.erp.asset.domain.repository;

import com.example.erp.asset.domain.model.Asset;

import java.util.Optional;

public interface AssetRepository {

    Asset save(Asset aggregate);

    Optional<Asset> findById(String id);
}