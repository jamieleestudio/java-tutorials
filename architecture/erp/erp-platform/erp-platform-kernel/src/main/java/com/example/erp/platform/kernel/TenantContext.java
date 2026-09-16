package com.example.erp.platform.kernel;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String currentTenantId() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}