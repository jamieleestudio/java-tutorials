package com.example.erp.platform.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpExternalSystemClient implements ExternalSystemClient {

    private final RestClient restClient;

    public HttpExternalSystemClient() {
        this.restClient = RestClient.create();
    }

    @Override
    public String system() {
        return "http";
    }

    @Override
    public String post(String path, String body) {
        return restClient.post().uri(path).body(body).retrieve().body(String.class);
    }
}