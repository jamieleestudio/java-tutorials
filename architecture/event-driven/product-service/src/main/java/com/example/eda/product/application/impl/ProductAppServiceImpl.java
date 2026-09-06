package com.example.eda.product.application.impl;

import com.example.eda.product.application.ProductAppService;
import com.example.eda.product.application.command.CreateProductCommand;
import com.example.eda.product.application.command.EventDeduplicationService;
import com.example.eda.product.application.command.EventPublisherService;
import com.example.eda.product.application.dto.ProductDto;
import com.example.eda.product.domain.DeductedOrderRepository;
import com.example.eda.product.domain.Product;
import com.example.eda.product.domain.ProductRepository;
import com.example.eda.shared.EntityNotFoundException;
import com.example.eda.shared.event.InventoryDeductedEvent;
import com.example.eda.shared.event.InventoryDeductFailedEvent;
import com.example.eda.shared.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductAppServiceImpl implements ProductAppService {

    private static final Logger log = LoggerFactory.getLogger(ProductAppServiceImpl.class);

    private final ProductRepository productRepository;
    private final DeductedOrderRepository deductedOrderRepository;
    private final EventPublisherService eventPublisherService;
    private final EventDeduplicationService eventDeduplicationService;

    public ProductAppServiceImpl(ProductRepository productRepository,
                                 DeductedOrderRepository deductedOrderRepository,
                                 EventPublisherService eventPublisherService,
                                 EventDeduplicationService eventDeduplicationService) {
        this.productRepository = productRepository;
        this.deductedOrderRepository = deductedOrderRepository;
        this.eventPublisherService = eventPublisherService;
        this.eventDeduplicationService = eventDeduplicationService;
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        Product product = Product.create(command.name(), command.price(), command.stock());
        productRepository.save(product);
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    @Override
    @Transactional
    public void deductForOrder(String eventId, OrderCreatedEvent event) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        try {
            // Pre-validate ALL lines, then deduct (no partial deduction on failure)
            for (OrderCreatedEvent.OrderLine line : event.lines()) {
                Product product = findProductOrThrow(line.productId());
                product.deductStock(line.quantity()); // throws if insufficient
            }
            for (OrderCreatedEvent.OrderLine line : event.lines()) {
                Product product = findProductOrThrow(line.productId());
                productRepository.save(product);
            }
            // Compensation bookkeeping: remember what was deducted for this order
            deductedOrderRepository.save(new DeductedOrderRepository.DeductedOrderRecord(
                    event.orderId(),
                    event.lines().stream()
                            .map(line -> new DeductedOrderRepository.DeductedOrderRecord.DeductedLine(line.productId(), line.quantity()))
                            .toList()
            ));
            eventPublisherService.publish(new InventoryDeductedEvent(event.orderId()));
            log.info("Inventory deducted for order {}", event.orderId());
        } catch (Exception e) {
            // Validation/deduction failure → tx-safe failure event
            eventPublisherService.publish(new InventoryDeductFailedEvent(event.orderId(), e.getMessage()));
            log.warn("Inventory deduction failed for order {}: {}", event.orderId(), e.getMessage());
        }
        eventDeduplicationService.markProcessed(eventId);
    }

    @Override
    @Transactional
    public void restockForOrder(String eventId, String orderId) {
        if (eventDeduplicationService.isDuplicate(eventId)) {
            return;
        }
        deductedOrderRepository.findById(orderId).ifPresent(deducted -> {
            for (DeductedOrderRepository.DeductedOrderRecord.DeductedLine line : deducted.lines()) {
                Product product = findProductOrThrow(line.productId());
                product.restock(line.quantity());
                productRepository.save(product);
            }
            deductedOrderRepository.delete(deducted);
            log.info("Restocked {} line(s) for cancelled order {}", deducted.lines().size(), orderId);
        });
        eventDeduplicationService.markProcessed(eventId);
    }

    // --- Sync catalog read (cross-context) ---

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(String productId) {
        Product product = findProductOrThrow(productId);
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(List<String> productIds) {
        return productRepository.findAllByIds(productIds).stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
                .toList();
    }

    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found: " + productId));
    }
}