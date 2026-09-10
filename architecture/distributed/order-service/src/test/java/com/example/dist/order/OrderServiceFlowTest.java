package com.example.dist.order;

import com.example.dist.order.application.OrderService;
import com.example.dist.order.application.command.CreateOrderCommand;
import com.example.dist.order.application.command.PayOrderCommand;
import com.example.dist.order.application.dto.OrderDto;
import com.example.dist.order.application.query.GetOrderByIdQuery;
import com.example.dist.payment.application.PaymentService;
import com.example.dist.product.application.ProductService;
import com.example.dist.product.application.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Provider-defined interfaces make cross-service testing trivial:
 * the remote PaymentService/ProductService are mocked — no running processes needed.
 * This is the same test style used in ⑤⑥ where remote services are Feign/K8s-DNS clients.
 */
@SpringBootTest
@Transactional
class OrderServiceFlowTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ProductService productService;

    @Test
    void create_and_pay_order_with_remote_services_mocked() {
        when(productService.getProducts(anyList())).thenReturn(List.of(
                new ProductDto("p-1", "Laptop", new BigDecimal("999.99"), 10)));
        when(paymentService.pay(any(), any())).thenReturn("payment-1");

        OrderDto order = orderService.createOrder(new CreateOrderCommand(
                "customer-1", List.of(new CreateOrderCommand.OrderLine("p-1", 2))));

        assertThat(order.status().name()).isEqualTo("CREATED");
        assertThat(order.totalAmount()).isEqualByComparingTo("1999.98");

        orderService.payOrder(new PayOrderCommand(order.orderId()));

        OrderDto paid = orderService.getOrderById(new GetOrderByIdQuery(order.orderId()));
        assertThat(paid.status().name()).isEqualTo("PAID");
    }
}