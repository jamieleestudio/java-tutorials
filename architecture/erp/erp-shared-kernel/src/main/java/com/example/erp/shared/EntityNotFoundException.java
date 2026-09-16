package com.example.erp.shared;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}