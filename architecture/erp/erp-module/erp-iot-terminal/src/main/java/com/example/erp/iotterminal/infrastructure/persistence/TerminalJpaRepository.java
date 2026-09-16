package com.example.erp.iotterminal.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalJpaRepository extends BaseJpaRepository<TerminalEntity, String> {
}