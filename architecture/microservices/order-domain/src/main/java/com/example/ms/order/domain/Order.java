package com.example.ms.order.domain;

import com.example.ms.order.domain.event.OrderCancelledEvent;
import com.example.ms.order.domain.event.OrderCreatedEvent;
import com.example.ms.order.domain.event.OrderPaidEvent;
import com.example.ms.shared.AggregateRoot;
import com.example.ms.shared.BusinessRuleViolationException;
import com.example.ms.shared.IdGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order aggregate root.
 * Contains core business logic and state transitions.
 * Pure POJO — does not depend on Spring, JPA, or web frameworks.
 */
public class Order extends AggregateRoot {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private String paymentId;

    private Order(String id, String customerId, List<OrderItem> items, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
    }

    /**
     * Factory method to create a new order.
     */
    public static Order create(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_ORDER", "Customer ID must not be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessRuleViolationException("INVALID_ORDER", "Order must contain at least one item");
        }

        String orderId = IdGenerator.newId();
        Order order = new Order(orderId, customerId, items, OrderStatus.CREATED);

        List<String> productIds = items.stream().map(OrderItem::getProductId).toList();
        BigDecimal total = items.stream()
                .map(OrderItem::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.raiseEvent(new OrderCreatedEvent(orderId, productIds, total, Instant.now().toEpochMilli()));
        return order;
    }

    /**
     * Reconstruct an order from persisted state (for repository implementation).
     */
    public static Order reconstitute(String id, String customerId, List<OrderItem> items,
                                      OrderStatus status, String paymentId) {
        Order order = new Order(id, customerId, items, status);
        order.paymentId = paymentId;
        return order;
    }

    /**
     * Mark the order as paid.
     */
    public void markAsPaid(String paymentId) {
        if (!status.canTransitionTo(OrderStatus.PAID)) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot pay order in status " + status);
        }
        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
        raiseEvent(new OrderPaidEvent(this.id, paymentId, Instant.now().toEpochMilli()));
    }

    /**
     * Cancel the order.
     */
    public void cancel(String reason) {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot cancel order in status " + status);
        }
        this.status = OrderStatus.CANCELLED;
        raiseEvent(new OrderCancelledEvent(this.id, reason, Instant.now().toEpochMilli()));
    }

    /**
     * Confirm the order (after payment is verified and inventory is deducted).
     */
    public void confirm() {
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new BusinessRuleViolationException("INVALID_TRANSITION",
                    "Cannot confirm order in status " + status);
        }
        this.status = OrderStatus.CONFIRMED;
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
}