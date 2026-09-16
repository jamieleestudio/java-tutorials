package com.example.erp.quality.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class QualityReport extends AggregateRoot {

    private final String id;
    private String name;

    public QualityReport(String id, String name) {
        this.id = id;
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("QualityReport name must not be blank");
        }
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}