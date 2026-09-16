package com.example.erp.moraleducation.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoralActivityJpaRepository extends BaseJpaRepository<MoralActivityEntity, String> {
}