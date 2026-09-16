package com.example.erp.attendance.domain.service;

public interface FaceRecognitionPort {

    boolean verify(String studentId, String faceToken);
}