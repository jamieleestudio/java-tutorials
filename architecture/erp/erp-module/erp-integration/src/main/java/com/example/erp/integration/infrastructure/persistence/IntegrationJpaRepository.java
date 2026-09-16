package com.example.erp.integration.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationJpaRepository extends BaseJpaRepository<IntegrationEntity, String> {
}