package com.example.monolithic;

import com.example.monolithic.order.application.OrderService;
import com.example.monolithic.order.application.command.CreateOrderCommand;
import com.example.monolithic.order.application.dto.OrderDto;
import com.example.monolithic.order.application.query.GetOrderByIdQuery;
import com.example.monolithic.product.application.ProductService;
import com.example.monolithic.product.application.command.CreateProductCommand;
import com.example.monolithic.product.application.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the full monolithic flow: create product → create order → pay order.
 */
@SpringBootTest
@Transactional
class MonolithicIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Test
    void full_order_flow() {
        ProductDto product = productService.createProduct(
                new CreateProductCommand("Laptop", new BigDecimal("999.99"), 10));

        CreateOrderCommand command = new CreateOrderCommand(
                "customer-1",
                List.of(new CreateOrderCommand.OrderLine(product.productId(), 2))
        );
        OrderDto order = orderService.createOrder(command);

        assertThat(order.orderId()).isNotBlank();
        assertThat(order.status().name()).isEqualTo("CREATED");
        assertThat(order.totalAmount()).isEqualByComparingTo("1999.98");

        orderService.payOrder(new com.example.monolithic.order.application.command.PayOrderCommand(order.orderId()));

        OrderDto paidOrder = orderService.getOrderById(new GetOrderByIdQuery(order.orderId()));
        assertThat(paidOrder.status().name()).isEqualTo("PAID");
    }
}