package com.example.ms.order.application.impl;

import com.example.ms.order.application.OrderService;
import com.example.ms.order.application.command.CancelOrderCommand;
import com.example.ms.order.application.command.CreateOrderCommand;
import com.example.ms.order.application.command.EventPublisherService;
import com.example.ms.order.application.command.PayOrderCommand;
import com.example.ms.order.application.dto.OrderDto;
import com.example.ms.order.application.query.GetOrderByIdQuery;
import com.example.ms.order.application.query.GetOrdersByCustomerQuery;
import com.example.ms.order.domain.Order;
import com.example.ms.order.domain.OrderItem;
import com.example.ms.order.domain.OrderRepository;
import com.example.ms.payment.application.PaymentService;
import com.example.ms.product.application.ProductService;
import com.example.ms.product.application.dto.ProductDto;
import com.example.ms.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link OrderService}.
 * Orchestrates use cases, manages transaction boundaries.
 * Depends on domain + cross-context service interfaces (provider-defined),
 * NOT on infrastructure directly.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final EventPublisherService eventPublisherService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductService productService,
                            PaymentService paymentService,
                            EventPublisherService eventPublisherService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.paymentService = paymentService;
        this.eventPublisherService = eventPublisherService;
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderCommand command) {
        List<ProductDto> products = productService.getProducts(
                command.lines().stream().map(CreateOrderCommand.OrderLine::productId).toList()
        );

        List<OrderItem> items = command.lines().stream()
                .map(line -> {
                    ProductDto product = products.stream()
                            .filter(p -> p.productId().equals(line.productId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("PRODUCT_NOT_FOUND",
                                    "Product not found: " + line.productId()));
                    return new OrderItem(product.productId(), product.name(),
                            line.quantity(), product.price());
                })
                .toList();

        Order order = Order.create(command.customerId(), items);
        orderRepository.save(order);

        publishEvents(order);

        return toDto(order);
    }

    @Override
    @Transactional
    public void payOrder(PayOrderCommand command) {
        Order order = findOrderOrThrow(command.orderId());
        String paymentId = paymentService.pay(command.orderId(), order.totalAmount());
        order.markAsPaid(paymentId);
        orderRepository.save(order);

        publishEvents(order);
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        Order order = findOrderOrThrow(command.orderId());
        if (order.getPaymentId() != null) {
            paymentService.refund(order.getPaymentId());
        }
        order.cancel(command.reason());
        orderRepository.save(order);

        publishEvents(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(GetOrderByIdQuery query) {
        Order order = findOrderOrThrow(query.orderId());
        return toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomer(GetOrdersByCustomerQuery query) {
        return orderRepository.findByCustomerId(query.customerId()).stream()
                .map(this::toDto)
                .toList();
    }

    private Order findOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("ORDER_NOT_FOUND",
                        "Order not found: " + orderId));
    }

    private void publishEvents(Order order) {
        order.getDomainEvents().forEach(eventPublisherService::publish);
        order.clearDomainEvents();
    }

    private OrderDto toDto(Order order) {
        List<OrderDto.OrderLineDto> lines = order.getItems().stream()
                .map(item -> new OrderDto.OrderLineDto(
                        item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice()))
                .toList();
        return new OrderDto(order.getId(), order.getCustomerId(),
                order.getStatus(), order.totalAmount(), lines);
    }
}