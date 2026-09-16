package com.example.erp.dormitory.domain.repository;

import com.example.erp.dormitory.domain.model.Dormitory;

import java.util.Optional;

public interface DormitoryRepository {

    Dormitory save(Dormitory aggregate);

    Optional<Dormitory> findById(String id);
}