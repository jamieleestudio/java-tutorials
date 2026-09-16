package com.example.erp.asset.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetJpaRepository extends BaseJpaRepository<AssetEntity, String> {
}