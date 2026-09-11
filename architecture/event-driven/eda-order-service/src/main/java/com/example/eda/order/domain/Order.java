package com.example.eda.order.domain;

import com.example.eda.shared.BusinessRuleViolationException;
import com.example.eda.shared.IdGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order aggregate root — saga state machine.
 * CONFIRMED only when BOTH async conditions arrive:
 * payment succeeded AND inventory deducted (eventual consistency).
 */
public class Order {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private String paymentId;
    private boolean paymentSucceeded;
    private boolean inventoryDeducted;

    private Order(String id, String customerId, List<OrderItem> items, OrderStatus status,
                  String paymentId, boolean paymentSucceeded, boolean inventoryDeducted) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.paymentId = paymentId;
        this.paymentSucceeded = paymentSucceeded;
        this.inventoryDeducted = inventoryDeducted;
    }

    public static Order create(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_ORDER", "Customer ID must not be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessRuleViolationException("INVALID_ORDER", "Order must contain at least one item");
        }
        return new Order(IdGenerator.newId(), customerId, items, OrderStatus.CREATED, null, false, false);
    }

    public static Order reconstitute(String id, String customerId, List<OrderItem> items,
                                      OrderStatus status, String paymentId,
                                      boolean paymentSucceeded, boolean inventoryDeducted) {
        return new Order(id, customerId, items, status, paymentId, paymentSucceeded, inventoryDeducted);
    }

    /**
     * Saga: PaymentSucceededEvent arrived.
     */
    public void markPaymentSucceeded(String paymentId) {
        if (status != OrderStatus.CREATED) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot record payment for order in status " + status);
        }
        this.paymentId = paymentId;
        this.paymentSucceeded = true;
        confirmIfReady();
    }

    /**
     * Saga: InventoryDeductedEvent arrived.
     */
    public void markInventoryDeducted() {
        if (status != OrderStatus.CREATED) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot record inventory for order in status " + status);
        }
        this.inventoryDeducted = true;
        confirmIfReady();
    }

    private void confirmIfReady() {
        if (paymentSucceeded && inventoryDeducted && status == OrderStatus.CREATED) {
            status = OrderStatus.CONFIRMED;
        }
    }

    /**
     * Saga compensation: cancel the order.
     */
    public void cancel(String reason) {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot cancel order in status " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public boolean isPaymentSucceeded() {
        return paymentSucceeded;
    }

    public boolean isInventoryDeducted() {
        return inventoryDeducted;
    }
}