package com.example.erp.integration.api;

import com.example.erp.integration.api.dto.IntegrationDto;

public interface IntegrationQueryApi {

    IntegrationDto findById(String id);
}