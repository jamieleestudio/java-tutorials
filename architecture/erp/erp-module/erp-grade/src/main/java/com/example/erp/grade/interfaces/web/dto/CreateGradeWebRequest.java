package com.example.erp.grade.interfaces.web.dto;

public record CreateGradeWebRequest(String studentId, String courseName, double score) {
}