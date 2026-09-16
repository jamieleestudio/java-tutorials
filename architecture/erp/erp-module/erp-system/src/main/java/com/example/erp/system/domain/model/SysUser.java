package com.example.erp.system.domain.model;

import com.example.erp.shared.AggregateRoot;
import com.example.erp.shared.BusinessRuleViolationException;

public class SysUser extends AggregateRoot {

    private final String id;
    private String name;

    public SysUser(String id, String name) {
        this.id = id;
        rename(name);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("SysUser name must not be blank");
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