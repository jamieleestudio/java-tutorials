package com.example.erp.platform.integration;

public interface ExternalSystemClient {

    String system();

    String post(String path, String body);
}