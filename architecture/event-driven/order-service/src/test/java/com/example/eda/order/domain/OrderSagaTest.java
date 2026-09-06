package com.example.eda.order.domain;

import com.example.eda.shared.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSagaTest {

    private Order createSampleOrder() {
        return Order.create("c1", List.of(
                new OrderItem("p1", "Laptop", 1, new BigDecimal("100"))));
    }

    @Test
    void order_starts_created_with_no_saga_progress() {
        Order order = createSampleOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.isPaymentSucceeded()).isFalse();
        assertThat(order.isInventoryDeducted()).isFalse();
    }

    @Test
    void single_saga_step_does_not_confirm() {
        Order order = createSampleOrder();
        order.markPaymentSucceeded("pay-1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        Order order2 = createSampleOrder();
        order2.markInventoryDeducted();
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void both_saga_steps_confirm_the_order() {
        Order order = createSampleOrder();
        order.markPaymentSucceeded("pay-1");
        order.markInventoryDeducted();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getPaymentId()).isEqualTo("pay-1");
    }

    @Test
    void inventory_after_payment_also_confirms() {
        Order order = createSampleOrder();
        order.markInventoryDeducted();
        order.markPaymentSucceeded("pay-1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cancel_from_created_allowed() {
        Order order = createSampleOrder();
        order.cancel("payment failed");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cannot_cancel_confirmed_order() {
        Order order = createSampleOrder();
        order.markPaymentSucceeded("pay-1");
        order.markInventoryDeducted();
        assertThatThrownBy(() -> order.cancel("too late"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cannot_record_payment_twice() {
        Order order = createSampleOrder();
        order.markPaymentSucceeded("pay-1");
        order.markInventoryDeducted(); // now CONFIRMED
        assertThatThrownBy(() -> order.markPaymentSucceeded("pay-2"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}