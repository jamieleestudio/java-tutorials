package com.example.eda.product.application;

import com.example.eda.product.application.command.CreateProductCommand;
import com.example.eda.product.application.dto.ProductDto;
import com.example.eda.shared.event.OrderCreatedEvent;

import java.util.List;

/**
 * Product side of the saga + sync catalog read (ProductService from product-api).
 */
public interface ProductAppService extends com.example.eda.product.application.ProductService {

    ProductDto createProduct(CreateProductCommand command);

    /**
     * OrderCreatedEvent → deduct stock → publish InventoryDeductedEvent
     * (or InventoryDeductFailedEvent on insufficient stock).
     */
    void deductForOrder(String eventId, OrderCreatedEvent event);

    /**
     * OrderCancelledEvent → restock what was deducted (saga compensation).
     */
    void restockForOrder(String eventId, String orderId);
}