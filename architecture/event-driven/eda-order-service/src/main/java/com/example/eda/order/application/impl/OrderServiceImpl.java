package com.example.eda.order.application.impl;

import com.example.eda.order.application.OrderService;
import com.example.eda.order.application.command.CancelOrderCommand;
import com.example.eda.order.application.command.CreateOrderCommand;
import com.example.eda.order.application.command.EventDeduplicationService;
import com.example.eda.order.application.command.EventPublisherService;
import com.example.eda.order.application.dto.OrderDto;
import com.example.eda.order.application.query.GetOrderByIdQuery;
import com.example.eda.order.application.query.GetOrdersByCustomerQuery;
import com.example.eda.order.domain.Order;
import com.example.eda.order.domain.OrderItem;
import com.example.eda.order.domain.OrderRepository;
import com.example.eda.product.application.ProductService;
import com.example.eda.product.application.dto.ProductDto;
import com.example.eda.shared.EntityNotFoundException;
import com.example.eda.shared.event.OrderCancelledEvent;
import com.example.eda.shared.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Order saga orchestrator side.
 *
 * createOrder: ONE local transaction = save order + write outbox message
 * (local message table pattern). Returns immediately — payment and inventory
 * happen async (eventual consistency), the saga completes via event reactions.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService; // sync catalog READ (pragmatic hybrid)
    private final EventPublisherService eventPublisherService;
    private final EventDeduplicationService eventDeduplicationService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductService productService,
                            EventPublisherService eventPublisherService,
                            EventDeduplicationService eventDeduplicationService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.eventPublisherService = eventPublisherService;
        this.eventDeduplicationService = eventDeduplicationService;
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

        // Same local tx: order + outbox message (atomic — no lost events)
        eventPublisherService.publish(new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderLine(
                                item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                order.totalAmount()
        ));

        return toDto(order);
    }

    @Override
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        Order order = findOrderOrThrow(command.orderId());
        order.cancel(command.reason());
        orderRepository.save(order);
        eventPublisherService.publish(new OrderCancelledEvent(command.orderId(), command.reason()));
    }

    // --- Saga reactions ---

    @Override
    @Transactional
    public void onPaymentSucceeded(String eventId, String orderId, String paymentId) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        Order order = findOrderOrThrow(orderId);
        order.markPaymentSucceeded(paymentId);
        orderRepository.save(order);
        eventDeduplicationService.markProcessed(eventId);
    }

    @Override
    @Transactional
    public void onPaymentFailed(String eventId, String orderId, String reason) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() == com.example.eda.order.domain.OrderStatus.CREATED) {
            order.cancel(reason);
            orderRepository.save(order);
            // Compensation: product-service will restock
            eventPublisherService.publish(new OrderCancelledEvent(orderId, reason));
        }
        eventDeduplicationService.markProcessed(eventId);
    }

    @Override
    @Transactional
    public void onInventoryDeducted(String eventId, String orderId) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        Order order = findOrderOrThrow(orderId);
        order.markInventoryDeducted();
        orderRepository.save(order);
        eventDeduplicationService.markProcessed(eventId);
    }

    @Override
    @Transactional
    public void onInventoryDeductFailed(String eventId, String orderId, String reason) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() == com.example.eda.order.domain.OrderStatus.CREATED) {
            order.cancel(reason);
            orderRepository.save(order);
            eventPublisherService.publish(new OrderCancelledEvent(orderId, reason));
        }
        eventDeduplicationService.markProcessed(eventId);
    }

    // --- Queries ---

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(GetOrderByIdQuery query) {
        return toDto(findOrderOrThrow(query.orderId()));
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

    private OrderDto toDto(Order order) {
        List<OrderDto.OrderLineDto> lines = order.getItems().stream()
                .map(item -> new OrderDto.OrderLineDto(
                        item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice()))
                .toList();
        return new OrderDto(order.getId(), order.getCustomerId(), order.getStatus(),
                order.totalAmount(), order.isPaymentSucceeded(), order.isInventoryDeducted(), lines);
    }
}