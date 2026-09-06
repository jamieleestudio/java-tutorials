package com.example.cn.order.domain;

import com.example.cn.shared.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void create_order_successfully() {
        OrderItem item = new OrderItem("p1", "Laptop", 2, new BigDecimal("999.99"));
        Order order = Order.create("c1", List.of(item));

        assertThat(order.getId()).isNotBlank();
        assertThat(order.getCustomerId()).isEqualTo("c1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.totalAmount()).isEqualByComparingTo("1999.98");
        assertThat(order.getDomainEvents()).hasSize(1);
    }

    @Test
    void create_order_with_empty_items_throws() {
        assertThatThrownBy(() -> Order.create("c1", List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void create_order_with_blank_customer_throws() {
        OrderItem item = new OrderItem("p1", "Laptop", 1, new BigDecimal("100"));
        assertThatThrownBy(() -> Order.create("", List.of(item)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void mark_as_paid_transitions_to_paid() {
        Order order = createSampleOrder();
        order.markAsPaid("pay-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentId()).isEqualTo("pay-1");
        assertThat(order.getDomainEvents()).hasSize(2);
    }

    @Test
    void cannot_pay_already_paid_order() {
        Order order = createSampleOrder();
        order.markAsPaid("pay-1");

        assertThatThrownBy(() -> order.markAsPaid("pay-2"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancel_created_order() {
        Order order = createSampleOrder();
        order.cancel("changed mind");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void confirm_paid_order() {
        Order order = createSampleOrder();
        order.markAsPaid("pay-1");
        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cannot_cancel_shipped_order() {
        Order order = createSampleOrder();
        order.markAsPaid("pay-1");
        order.confirm();

        assertThatThrownBy(() -> order.cancel("reason"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Order createSampleOrder() {
        OrderItem item = new OrderItem("p1", "Laptop", 1, new BigDecimal("100"));
        return Order.create("c1", List.of(item));
    }
}