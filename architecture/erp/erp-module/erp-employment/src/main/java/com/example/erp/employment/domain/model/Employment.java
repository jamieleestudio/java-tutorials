package com.example.erp.employment.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class Employment extends AggregateRoot {

    private final String id;
    private String name;

    public Employment(String id, String name) {
        this.id = id;
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("Employment name must not be blank");
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