package com.example.eda.product.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface DeductedOrderJpaRepository extends JpaRepository<DeductedOrderEntity, String> {
}