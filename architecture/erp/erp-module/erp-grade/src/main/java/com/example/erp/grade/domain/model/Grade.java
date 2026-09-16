package com.example.erp.grade.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class Grade extends AggregateRoot {

    private final String id;
    private final String studentId;
    private final String courseName;
    private double score;

    public Grade(String id, String studentId, String courseName, double score) {
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessRuleViolationException("studentId must not be blank");
        }
        if (courseName == null || courseName.isBlank()) {
            throw new BusinessRuleViolationException("courseName must not be blank");
        }
        this.id = id;
        this.studentId = studentId;
        this.courseName = courseName;
        this.score = score;
        validate(score);
    }

    public void updateScore(double score) {
        validate(score);
        this.score = score;
    }

    public boolean passed() {
        return score >= 60;
    }

    private void validate(double score) {
        if (score < 0 || score > 100) {
            throw new BusinessRuleViolationException("score must be between 0 and 100");
        }
    }

    public String id() {
        return id;
    }

    public String studentId() {
        return studentId;
    }

    public String courseName() {
        return courseName;
    }

    public double score() {
        return score;
    }
}