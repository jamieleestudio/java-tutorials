package com.example.monolithic.shared;

/**
 * Thrown when a business rule is violated.
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
}