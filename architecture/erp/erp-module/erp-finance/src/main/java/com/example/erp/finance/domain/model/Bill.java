package com.example.erp.finance.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class Bill extends AggregateRoot {

    private final String id;
    private String name;

    public Bill(String id, String name) {
        this.id = id;
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("Bill name must not be blank");
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