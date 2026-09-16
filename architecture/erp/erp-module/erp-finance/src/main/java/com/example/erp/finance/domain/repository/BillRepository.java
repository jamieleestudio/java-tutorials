package com.example.erp.finance.domain.repository;

import com.example.erp.finance.domain.model.Bill;

import java.util.Optional;

public interface BillRepository {

    Bill save(Bill aggregate);

    Optional<Bill> findById(String id);
}