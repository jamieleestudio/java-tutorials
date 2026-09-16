package com.example.erp.iotterminal.domain.repository;

import com.example.erp.iotterminal.domain.model.Terminal;

import java.util.Optional;

public interface TerminalRepository {

    Terminal save(Terminal aggregate);

    Optional<Terminal> findById(String id);
}