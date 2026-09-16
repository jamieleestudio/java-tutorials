package com.example.erp.asset.infrastructure.persistence;

import com.example.erp.asset.domain.model.Asset;
import com.example.erp.asset.domain.repository.AssetRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetJpaRepository jpaRepository;

    public AssetRepositoryImpl(AssetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Asset save(Asset aggregate) {
        AssetEntity entity = new AssetEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        AssetEntity saved = jpaRepository.save(entity);
        return new Asset(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Asset> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Asset(e.getId(), e.getName()));
    }
}