package com.example.eda.product.domain;

import java.util.List;
import java.util.Optional;

/**
 * Saga compensation bookkeeping port: what was deducted for an order.
 */
public interface DeductedOrderRepository {

    void save(DeductedOrderRecord record);

    Optional<DeductedOrderRecord> findById(String orderId);

    void delete(DeductedOrderRecord record);

    record DeductedOrderRecord(String orderId, List<DeductedLine> lines) {
        public record DeductedLine(String productId, int quantity) {
        }
    }
}