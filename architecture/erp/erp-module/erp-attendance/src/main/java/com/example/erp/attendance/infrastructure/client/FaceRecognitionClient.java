package com.example.erp.attendance.infrastructure.client;

import com.example.erp.attendance.domain.service.FaceRecognitionPort;
import com.example.erp.platform.integration.ExternalSystemClient;
import org.springframework.stereotype.Component;

@Component
public class FaceRecognitionClient implements FaceRecognitionPort {

    private final ExternalSystemClient externalSystemClient;

    public FaceRecognitionClient(ExternalSystemClient externalSystemClient) {
        this.externalSystemClient = externalSystemClient;
    }

    @Override
    public boolean verify(String studentId, String faceToken) {
        return faceToken != null && faceToken.startsWith("face-") && externalSystemClient.system() != null;
    }
}