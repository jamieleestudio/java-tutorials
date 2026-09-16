package com.example.erp.platform.kernel;

public final class ErrorCode {

    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String NOT_FOUND = "404";
    public static final String BUSINESS_RULE = "422";
    public static final String INTERNAL = "500";

    private ErrorCode() {
    }
}