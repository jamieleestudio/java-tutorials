package com.example.monolithic.shared;

/**
 * Thrown when an entity is not found.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
}