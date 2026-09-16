package com.example.erp.moraleducation.domain.repository;

import com.example.erp.moraleducation.domain.model.MoralActivity;

import java.util.Optional;

public interface MoralActivityRepository {

    MoralActivity save(MoralActivity aggregate);

    Optional<MoralActivity> findById(String id);
}