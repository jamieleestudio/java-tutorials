package com.example.erp.enrollment.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class Enrollment extends AggregateRoot {

    private final String id;
    private String name;

    public Enrollment(String id, String name) {
        this.id = id;
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("Enrollment name must not be blank");
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