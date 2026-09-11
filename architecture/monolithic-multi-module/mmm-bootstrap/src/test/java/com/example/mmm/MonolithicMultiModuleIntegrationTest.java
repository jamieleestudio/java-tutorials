package com.example.mmm;

import com.example.mmm.bootstrap.MonolithicMultiModuleApplication;
import com.example.mmm.order.application.OrderService;
import com.example.mmm.order.application.command.CreateOrderCommand;
import com.example.mmm.order.application.command.PayOrderCommand;
import com.example.mmm.order.application.dto.OrderDto;
import com.example.mmm.order.application.query.GetOrderByIdQuery;
import com.example.mmm.product.application.ProductService;
import com.example.mmm.product.application.command.CreateProductCommand;
import com.example.mmm.product.application.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-flow integration test inside the assembled monolith (1 JVM).
 */
@SpringBootTest(classes = MonolithicMultiModuleApplication.class)
@Transactional
class MonolithicMultiModuleIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Test
    void full_order_flow_across_modules() {
        ProductDto product = productService.createProduct(
                new CreateProductCommand("Laptop", new BigDecimal("999.99"), 10));

        OrderDto order = orderService.createOrder(new CreateOrderCommand(
                "customer-1",
                List.of(new CreateOrderCommand.OrderLine(product.productId(), 2))
        ));

        assertThat(order.status().name()).isEqualTo("CREATED");
        assertThat(order.totalAmount()).isEqualByComparingTo("1999.98");

        orderService.payOrder(new PayOrderCommand(order.orderId()));

        OrderDto paid = orderService.getOrderById(new GetOrderByIdQuery(order.orderId()));
        assertThat(paid.status().name()).isEqualTo("PAID");
    }
}