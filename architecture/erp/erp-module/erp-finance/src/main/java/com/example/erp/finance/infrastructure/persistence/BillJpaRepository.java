package com.example.erp.finance.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillJpaRepository extends BaseJpaRepository<BillEntity, String> {
}