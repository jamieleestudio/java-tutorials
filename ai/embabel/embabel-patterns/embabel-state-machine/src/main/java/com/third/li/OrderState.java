package com.third.li;

/**
 * 订单处理的状态集合。
 *
 * <pre>
 *   NEW --validateOrder--> VALIDATED --chargePayment--> PAID --shipOrder--> SHIPPED
 *    |
 *    +---rejectOrder--> REJECTED
 * </pre>
 */
public enum OrderState {
    NEW,
    VALIDATED,
    PAID,
    SHIPPED,
    REJECTED
}
