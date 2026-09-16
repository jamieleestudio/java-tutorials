package com.example.erp.shared;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}