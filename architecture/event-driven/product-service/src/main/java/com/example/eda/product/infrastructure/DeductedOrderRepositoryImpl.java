package com.example.eda.product.infrastructure;

import com.example.eda.product.domain.DeductedOrderRepository;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the saga compensation bookkeeping port.
 */
@Component
public class DeductedOrderRepositoryImpl implements DeductedOrderRepository {

    private final DeductedOrderJpaRepository jpaRepository;

    public DeductedOrderRepositoryImpl(DeductedOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(DeductedOrderRecord record) {
        List<DeductedLineEmbeddable> lines = record.lines().stream()
                .map(line -> new DeductedLineEmbeddable(line.productId(), line.quantity()))
                .toList();
        jpaRepository.save(new DeductedOrderEntity(record.orderId(), lines));
    }

    @Override
    public Optional<DeductedOrderRecord> findById(String orderId) {
        return jpaRepository.findById(orderId)
                .map(entity -> new DeductedOrderRecord(
                        entity.getOrderId(),
                        entity.getLines().stream()
                                .map(line -> new DeductedOrderRepository.DeductedOrderRecord.DeductedLine(
                                        line.productId(), line.quantity()))
                                .toList()));
    }

    @Override
    public void delete(DeductedOrderRecord record) {
        jpaRepository.findById(record.orderId()).ifPresent(jpaRepository::delete);
    }
}

@Entity
@Table(name = "deducted_orders")
@Getter
@Setter
@NoArgsConstructor
class DeductedOrderEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @ElementCollection
    @CollectionTable(name = "deducted_order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<DeductedLineEmbeddable> lines = new ArrayList<>();

    DeductedOrderEntity(String orderId, List<DeductedLineEmbeddable> lines) {
        this.orderId = orderId;
        this.lines = lines;
    }
}

record DeductedLineEmbeddable(
        @Column(name = "product_id", nullable = false) String productId,
        @Column(name = "quantity", nullable = false) int quantity
) {
}