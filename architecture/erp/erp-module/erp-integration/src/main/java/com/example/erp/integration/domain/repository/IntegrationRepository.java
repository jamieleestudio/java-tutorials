package com.example.erp.integration.domain.repository;

import com.example.erp.integration.domain.model.Integration;

import java.util.Optional;

public interface IntegrationRepository {

    Integration save(Integration aggregate);

    Optional<Integration> findById(String id);
}