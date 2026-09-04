package com.example.monolithic.shared;

import java.util.UUID;

/**
 * Utility for generating IDs. Pure Java, no framework dependency.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}